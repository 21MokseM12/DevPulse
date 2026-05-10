package scrapper.bot.connectivity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;

public class ValidUriValidator implements ConstraintValidator<ValidUri, URI> {

    @Override
    public boolean isValid(URI value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.isAbsolute()
                && value.getScheme() != null
                && !value.getScheme().isBlank();
    }
}
