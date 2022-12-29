package ibpe.io;

import java.util.*;

public interface IErrorReporter {
    public void reportError(String error);
    public Collection<String> getErrors();
}
