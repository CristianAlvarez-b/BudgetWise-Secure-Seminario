class FinancialModel {
    constructor() {
        this.incomes = [];
        this.expenses = [];
        this.movements = [];
        this.questions = [];
        this.answers = [];
        this.balance = 0;
        //this.token = localStorage.getItem('token'); // Suponiendo que el token está guardado en localStorage
        this.APIURL = "/api/";
        this.secretKey = 'budgetWise';
        this.initialize();

    }
    async initialize() {
        try {
            await this.loadToken(); // Asegura que el token esté cargado primero
            console.log('Token cargado:', this.token);

            // Una vez cargado el token, ejecuta las acciones que lo necesitan
            await this.loadMovements();
            await this.loadQuestions();
        } catch (error) {
            console.error('Error durante la inicialización:', error);
        }
    }
    async loadToken() {
        try {
            const tokenData = await this.getTokenDecrypted();
            if (tokenData) {
                this.token = tokenData.jwt; // Accede al JWT
                this.csrfToken = tokenData.csrfToken; // Accede al CSRF Token
                console.log('Token loaded from IndexedDB:', this.token);
            } else {
                console.error('No token found in IndexedDB');
            }
        } catch (error) {
            console.error('Error loading token from IndexedDB:', error);
        }
    }
    decryptData(cipherText) {
        const bytes = CryptoJS.AES.decrypt(cipherText, this.secretKey);
        return JSON.parse(bytes.toString(CryptoJS.enc.Utf8));
    }
    getTokenDecrypted() {
        return this.openDatabase().then((db) => {
            return new Promise((resolve, reject) => {
                const transaction = db.transaction('tokens', 'readonly');
                const store = transaction.objectStore('tokens');
                const request = store.get('auth');

                request.onsuccess = () => {
                    if (request.result) {
                        resolve(this.decryptData(request.result.data));
                    } else {
                        resolve(null);
                    }
                };
                request.onerror = (event) => reject(event.target.error);
            });
        });
    }
    openDatabase() {
        return new Promise((resolve, reject) => {
            const request = indexedDB.open('TokenDB', 1);

            // Crear el almacén de objetos si es necesario
            request.onupgradeneeded = (event) => {
                const db = event.target.result;
                db.createObjectStore('tokens', { keyPath: 'id' });
            };

            request.onsuccess = (event) => resolve(event.target.result);
            request.onerror = (event) => reject(event.target.error);
        });
    }
    async loadMovements() {
        try {
            const response = await fetch(`${this.APIURL}movements/`, {
                headers: {
                    'Authorization': this.token,
                    'X-CSRF-Token': this.csrfToken,
                    'Content-Type': 'application/json'
                }
            });
            if (!response.ok) throw new Error('Error al obtener los movimientos');
            const data = await response.json();

            // Limpiar los arrays antes de cargar nuevos movimientos
            this.movements = [];

            // Separar movimientos según su tipo
            data.forEach(item => {
                const movement = {
                    id: item.id,
                    name: item.name,
                    value: item.value,
                    date: item.date,
                    type: item.type
                };
                this.movements.push(movement);

            });
            this.updateBalance();
        } catch (error) {
            console.error('Error al cargar movimientos:', error);
            this.movements = [];
        }
    }

    async addIncome(name, value, date) {
        const income = { name, value, date };
        await this.saveMovement('income', income);
        this.incomes.unshift(income);
        this.updateBalance();
    }

    async addExpense(name, value, date) {
        const expense = { name, value, date };
        await this.saveMovement('outcome', expense);
        this.expenses.unshift(expense);
        this.updateBalance();
    }

    async addQuestion(questionText) {
        try {
            const question = { text: questionText };

            const response = await fetch(`${this.APIURL}questions/`, {
                method: 'POST',
                headers: {
                    'Authorization': this.token,
                    'X-CSRF-Token': this.csrfToken,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(question)
            });
            if (!response.ok) {
                const responseText = await response.text();
                throw new Error(`Error al guardar la pregunta: ${responseText}`);
            }
            Swal.fire({
                icon: 'success',
                title: 'Question added successfully!',
                showConfirmButton: false,
                timer: 2500
            });
        } catch (error) {
            console.error('Error al agregar pregunta:', error);
            this.questions = [];
        }
    }

    async addAnswer(questionId, answerText) {
        try {
            const token = this.token;
            const body = { questionId, text: answerText };

            const response = await fetch(`${this.APIURL}answers`, {
                method: 'POST',
                headers: {
                    'Authorization': this.token,
                    'X-CSRF-Token': this.csrfToken,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(body)
            });

            if (!response.ok) {
                throw new Error('Error al agregar la respuesta');
            }
            Swal.fire({
                icon: 'success',
                title: 'Answer added successfully!',
                showConfirmButton: false,
                timer: 2500
            });
        } catch (error) {
            console.error('Error en addAnswer:', error);
        }
    }

    async loadQuestions() {
        try {
            const token = this.token;
            const response = await fetch(`${this.APIURL}questions/`, {
                headers: {
                    'Authorization': this.token,
                    'X-CSRF-Token': this.csrfToken,
                    'Content-Type': 'application/json'
                }
            });
            if (!response.ok) throw new Error('Error al obtener las preguntas');
            const data = await response.json();

            this.questions = [];
            for (const item of data) {
                const question = {
                    id: item.id,
                    username: item.username,
                    text: item.text,
                    answers: []
                };
                await this.loadAnswersForQuestion(question.id, question);
                this.questions.push(question);
            }
        } catch (error) {
            console.error('Error al cargar preguntas:', error);
            this.questions = [];
        }
    }

    async loadAnswersForQuestion(questionId, question) {
        try {
            const token = this.token;
            const response = await fetch(`${this.APIURL}answers/${questionId}`, {
                headers: {
                    'Authorization': this.token,
                    'X-CSRF-Token': this.csrfToken,
                    'Content-Type': 'application/json'
                }
            });
            if (!response.ok) throw new Error('Error al obtener respuestas');
            const answers = await response.json();

            question.answers = answers.map(answer => ({
                id: answer.id,
                text: answer.text,
                username: answer.username
            }));
        } catch (error) {
            console.error(`Error al cargar respuestas para la pregunta ${questionId}:`, error);
            question.answers = [];
        }
    }

    async saveMovement(type, movement) {
        try {
            const token = this.token;
            const dateInISOFormat = movement.date.replace(' ', 'T');

            const body = {
                name: movement.name,
                value: movement.value,
                date: dateInISOFormat,
                type: type
            };

            const response = await fetch(`${this.APIURL}movements/`, {
                method: 'POST',
                headers: {
                    'Authorization': this.token,
                    'X-CSRF-Token': this.csrfToken,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(body)
            });

            if (!response.ok) {
                const responseText = await response.text();
                throw new Error(`Error al guardar el movimiento: ${responseText}`);
            }

            await this.loadMovements();
        } catch (error) {
            console.error('Error saving movement:', error);
        }
    }

    async updateMovement(realIndex, updatedMovement) {
        try {
            const token = this.token;
            const response = await fetch(`${this.APIURL}movements/${realIndex}`, {
                method: 'PUT',
                headers: {
                    'Authorization': this.token,
                    'X-CSRF-Token': this.csrfToken,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(updatedMovement)
            });

            if (!response.ok) {
                throw new Error('Error al actualizar el movimiento');
            }
            await this.loadMovements();
        } catch (error) {
            console.error('Error en la actualización del movimiento:', error);
            throw error;
        }
    }

    async deleteMovement(id) {
        try {
            const token = this.token;
            const response = await fetch(`${this.APIURL}movements/${id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': this.token,
                    'X-CSRF-Token': this.csrfToken,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                const responseText = await response.text();
                throw new Error(`Error al eliminar el movimiento: ${responseText}`);
            }

            await this.loadMovements();
        } catch (error) {
            console.error('Error al eliminar el movimiento:', error);
        }
    }
    async transferMoney(userName, value) {
        try {
            const token = this.token;
            const formData = new FormData();
            formData.append('targetUsername', userName);
            formData.append('amount', value);

            // Realiza la solicitud POST al endpoint de transferencias
            const response = await fetch(`${this.APIURL}movements/transfer`, {
                method: 'POST',
                headers: {
                    'Authorization': this.token,
                    'X-CSRF-Token': this.csrfToken,
                    // 'Content-Type' no se necesita porque FormData lo maneja automáticamente
                },
                body: formData, // Enviar los datos como FormData
            });

            // Si la respuesta no es exitosa, lanza un error
            if (!response.ok) {
                const responseText = await response.text();
                throw new Error(`Error en la transferencia: ${responseText}`);
            }

            // Si la transferencia es exitosa, actualiza los movimientos y el balance
            await this.loadMovements();
            Swal.fire({
                icon: 'success',
                title: 'Transferencia realizada con éxito!',
                showConfirmButton: false,
                timer: 2500,
            });

        } catch (error) {
            console.error('Error al realizar la transferencia:', error);
            Swal.fire({
                icon: 'error',
                title: 'Error al realizar la transferencia',
                text: error.message,
            });
        }
    }

    updateBalance() {
        let balance = 0; // Inicializa el balance en 0

        // Itera sobre los movimientos
        for (const movement of this.movements) {
            if (movement.type === 'income' || movement.type === 'pocketOutcome' || movement.type === 'transferIn') {
                balance += movement.value; // Suma al balance si es ingreso
            } else if (movement.type === 'outcome' || movement.type === 'pocketIncome' || movement.type === 'transferOut') {
                balance -= movement.value; // Resta al balance si es gasto
            }
        }

        // Guarda el balance actualizado en el localStorage (o cualquier otro sistema de persistencia)
        localStorage.setItem('balance', balance);

        // También puedes actualizar una variable de instancia si es necesario
        this.balance = balance;
    }

    getMovements() {
        return this.movements;
    }

    getQuestions() {
        return this.questions;
    }
}
export default FinancialModel;
