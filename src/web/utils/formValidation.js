import _ from 'lodash';

const formValidation = {
  checkInputValidity: (input, customErrorMessage) => {
    const { validity } = input;
    const isValid = validity.valid;

    if (isValid) {
      return '';
    }
    const isEmpty = validity.valueMissing;
    const isIncorrectType = validity.typeMismatch;
    const isIncorrectPattern = validity.patternMismatch;
    const isTooLong = validity.tooLong || validity.rangeOverflow;
    const isTooShort = validity.rangeUnderflow;
    const isInvalidStep = validity.stepMismatch;

    const possibleErrors = [
      { invalid: isEmpty, message: 'A value is required.' },
      { invalid: isIncorrectType, message: 'Please verify that your input is the expected type (ie: email address).' },
      { invalid: isIncorrectPattern, message: 'This input does not match the expected value.' },
      { invalid: isTooLong, message: 'Your input is too long.' },
      { invalid: isTooShort, message: 'Your input is too short.' },
      { invalid: isInvalidStep, message: 'Unexpected value.' },
    ];

    const errorOutput = _.find(possibleErrors, error => error.invalid);

    return customErrorMessage || errorOutput.message;
  },

  isFormValid: (form) => {
    if (!form.checkValidity()) {
      form.reportValidity();

      return false;
    }

    return true;
  },
};

export default formValidation;
