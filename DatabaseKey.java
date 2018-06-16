package chkrr00k.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.processing.SupportedAnnotationTypes;


/**
 * Rapresents the primary key or the foreign key equivalent in the database inside the bean object.<br/>
 * It can be used only on properties.
 * 
 * @author chkrr00k
 */
@Retention(RetentionPolicy.RUNTIME)
@SupportedAnnotationTypes(value = { "Type", "Table" })
@Target(ElementType.FIELD)
public @interface DatabaseKey {
	/**
	 * They key type.
	 * @see DatabaseKey.Type
	 */
	public Type Type() default Type.PRIMARY;
	/**
	 * The name of the table which the foreign key is from.<br/>
	 * Defaults at {@code<table>}
	 */
	public String Table() default "<table>";
	
	/**
	 * Describes the type of the constraint on the database.
	 * @author chkrr00k
	 */
	public enum Type {
			PRIMARY, FOREIGN
	}
}
