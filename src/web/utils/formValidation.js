const formValidation = {
  isFormValid: (form) => {
    if (!form.checkValidity()) {
      form.reportValidity();

      return false;
    }

    return true;
  },
};

export default formValidation;
