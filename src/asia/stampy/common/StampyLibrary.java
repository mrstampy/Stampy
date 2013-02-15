package asia.stampy.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@StampyLibrary(libraryName="stampy-core")
public @interface StampyLibrary {
  String libraryName();
}
