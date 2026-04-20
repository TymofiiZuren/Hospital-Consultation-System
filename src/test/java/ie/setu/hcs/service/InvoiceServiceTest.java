package ie.setu.hcs.service;

import ie.setu.hcs.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceServiceTest {

    private final InvoiceService service = new InvoiceService();

    @Test
    void normalizeStatusUppercasesSupportedValues() throws Exception {
        assertEquals("PAID", invokeString("normalizeStatus", new Class[]{String.class}, "paid"));
        assertEquals("UNPAID", invokeString("normalizeStatus", new Class[]{String.class}, " unpaid "));
    }

    @Test
    void normalizeStatusRejectsBlankOrUnsupportedValues() throws Exception {
        ValidationException blank = invokeValidation("normalizeStatus", new Class[]{String.class}, " ");
        assertEquals("Please choose an invoice status.", blank.getMessage());

        ValidationException invalid = invokeValidation("normalizeStatus", new Class[]{String.class}, "PARTIAL");
        assertEquals("Unsupported invoice status.", invalid.getMessage());
    }

    @Test
    void requireAmountAcceptsPositiveValue() throws Exception {
        Method method = InvoiceService.class.getDeclaredMethod("requireAmount", Float.class);
        method.setAccessible(true);
        float amount = (float) method.invoke(service, 25.5f);
        assertEquals(25.5f, amount);
    }

    @Test
    void requireAmountRejectsNullOrNonPositiveValues() throws Exception {
        ValidationException nullAmount = invokeValidation("requireAmount", new Class[]{Float.class}, new Object[]{null});
        assertEquals("Amount must be greater than zero.", nullAmount.getMessage());

        ValidationException zeroAmount = invokeValidation("requireAmount", new Class[]{Float.class}, 0f);
        assertEquals("Amount must be greater than zero.", zeroAmount.getMessage());
    }

    private String invokeString(String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = InvoiceService.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return (String) method.invoke(service, args);
    }

    private ValidationException invokeValidation(String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = InvoiceService.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        try {
            method.invoke(service, args);
            fail("Expected ValidationException");
            return null;
        } catch (InvocationTargetException ex) {
            assertInstanceOf(ValidationException.class, ex.getCause());
            return (ValidationException) ex.getCause();
        }
    }
}
