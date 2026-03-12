class BadRequestException extends Error {
    constructor(message, validationErrors = null) {
        super(message);
        this.validationErrors = validationErrors;
    }
}
export default BadRequestException;