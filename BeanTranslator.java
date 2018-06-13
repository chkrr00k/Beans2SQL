package chkrr00k.beans2sql;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.soap.util.Bean;
import org.apache.soap.util.StringUtils;

import chkrr00k.beans.Autore;
import chkrr00k.beans.Libro;
import chkrr00k.beans.Persona;

/**
 * 
 * A simple way to translate a java bean object into its own SQL query.<br/><br/>
 * <b>Example</b><br/>
 * {@code Object o = new Persona();} Given the bean object.<br/>
 * {@code BeanTranslator bt = new BeanTranslator();}<br/>
 * {@code bt.createTable(o);}<br/>
 * {@code bt.insertTable(o);}<br/>
 * {@code bt.deleteTable(o);}<br/>
 * {@code bt.selectTable(o);}<br/>
 * {@code bt.updateTable(o);}<br/>
 * {@code bt.deleteByIdTable(o);}<br/>
 * {@code bt.selectByIdTable(o);}<br/>
 * {@code bt.updateByIdTable(o);}<br/>
 * {@code bt.selectAllTable(o);}<br/>
 * {@code bt.dropTable(o);}<br/>
 * 
 * @author chkrr00k
 *
 */
public class BeanTranslator {
	private static final Map<String, String> TYPETRANSLATION = new HashMap<String, String>();

	static {
		TYPETRANSLATION.put("java.lang.String", "VARCHAR(100)");
		TYPETRANSLATION.put("int", "INT");
		TYPETRANSLATION.put("double", "DOUBLE");
		TYPETRANSLATION.put("float", "FLOAT");
		TYPETRANSLATION.put("char", "CHAR");

	}

	private static <T> Iterable<T> iteratorToIterable(Iterator<T> it) {
		return () -> it;
	}

	private List<Entry<String, String>> getFields(Object o) {
		List<Entry<String, String>> fields = new LinkedList<Entry<String, String>>();
		for (Field f : o.getClass().getDeclaredFields()) {
			/*
			 * System.out.println( f.getName() + " = " + o.getClass()
			 * .getMethod( "get" + ("" + f.getName().charAt(0)).toUpperCase() +
			 * f.getName().substring(1)) .invoke(o) );
			 */
			fields.add(new AbstractMap.SimpleEntry<String, String>(f.getName(), f.getType().getName()));
		}
		return fields;
	}

	private List<String> getPrimaryKeys(Object o) {
		List<String> pk = new LinkedList<String>();
		for (Field f : o.getClass().getDeclaredFields()) {
			if (f.getAnnotation(DatabaseKey.class) != null) {
				if (f.getAnnotation(DatabaseKey.class).Type().equals(DatabaseKey.Type.PRIMARY)) {
					pk.add(f.getName());
				}
			}
		}
		return pk;
	}

	private Map<String, String> getForeignKeys(Object o) {
		Map<String, String> fk = new HashMap<String, String>();
		for (Field f : o.getClass().getDeclaredFields()) {
			if (f.getAnnotation(DatabaseKey.class) != null) {
				if (f.getAnnotation(DatabaseKey.class).Type().equals(DatabaseKey.Type.FOREIGN)) {
					fk.put(f.getName(), f.getAnnotation(DatabaseKey.class).Table());
				}
			}
		}
		return fk;
	}

	/**
	 * Generates the create table query for the given java bean object.
	 * 
	 * @param o
	 *            The bean to create the query from.
	 * @return The create table query for the object given.
	 */
	public String createTable(Object o) {
		List<Entry<String, String>> fields = this.getFields(o);
		final StringBuilder strBld = new StringBuilder();

		strBld.append("CREATE TABLE ");
		strBld.append(o.getClass().getSimpleName());
		strBld.append(" (\n");

		strBld.append(String.join(",\n", iteratorToIterable(fields.stream().map((e) -> {
			try {
				return "\t" + e.getKey() + " " + BeanTranslator.TYPETRANSLATION.get(e.getValue());
			} catch (Exception ex) {
				throw new IllegalArgumentException("Invalid type");
			}
		}).iterator())));
		List<String> pk = this.getPrimaryKeys(o);
		Map<String, String> fk = this.getForeignKeys(o);

		if (pk.size() > 0) {
			strBld.append(",\n\tPRIMARY KEY ( ");
			strBld.append(String.join(", ", pk));
			strBld.append(" )");
		}
		if (fk.size() > 0) {
			fk.keySet().stream().forEach((e) -> {
				strBld.append(",\n\tFOREIGN KEY ( ");
				strBld.append(e);
				strBld.append(" ) REFERENCES ( ");
				strBld.append(fk.get(e));
				strBld.append(" )");
			});
		}
		strBld.append("\n)");
		return strBld.toString();
	}

	/**
	 * Generates the insert table query for the given java bean object.
	 * 
	 * @param o
	 *            The bean to create the query from.
	 * @return The insert table query for the object given.
	 */
	public String insertTable(Object o) {
		List<Entry<String, String>> fields = this.getFields(o);
		final StringBuilder strBld = new StringBuilder();

		strBld.append("INSERT INTO ");
		strBld.append(o.getClass().getSimpleName());
		strBld.append(" ( ");

		strBld.append(String.join(", ", iteratorToIterable(fields.stream().map((e) -> {
			return e.getKey();
		}).iterator())));

		strBld.append(" ) VALUES ( ");
		String[] questions = new String[fields.size()];
		Arrays.fill(questions, "?");
		strBld.append(String.join(", ", questions));
		strBld.append(" ) ");

		return strBld.toString();
	}

	/**
	 * Generates the delete table query for the given java bean object.
	 * 
	 * @param o
	 *            The bean to create the query from.
	 * @return The delete table query for the object given.
	 */
	public String deleteTable(Object o) {
		List<Entry<String, String>> fields = this.getFields(o);
		final StringBuilder strBld = new StringBuilder();

		strBld.append("DELETE FROM ");
		strBld.append(o.getClass().getSimpleName());
		strBld.append(" WHERE ");

		strBld.append(String.join(" AND ", iteratorToIterable(fields.stream().map((e) -> {
			return e.getKey() + " = ?";
		}).iterator())));

		return strBld.toString();
	}

	/**
	 * Generates the select from table query for the given java bean object.
	 * 
	 * @param o
	 *            The bean to create the query from.
	 * @return The select table query for the object given.
	 */
	public String selectTable(Object o) {
		List<Entry<String, String>> fields = this.getFields(o);
		final StringBuilder strBld = new StringBuilder();

		strBld.append("SELECT * FROM ");
		strBld.append(o.getClass().getSimpleName());
		strBld.append(" WHERE ");

		strBld.append(String.join(" AND ", iteratorToIterable(fields.stream().map((e) -> {
			return e.getKey() + " = ?";
		}).iterator())));

		return strBld.toString();
	}

	/**
	 * Generates the update table query for the given java bean object.
	 * 
	 * @param o
	 *            The bean to create the query from.
	 * @return The update table query for the object given.
	 */
	public String updateTable(Object o) {
		List<Entry<String, String>> fields = this.getFields(o);
		final StringBuilder strBld = new StringBuilder();

		strBld.append("UPDATE ");
		strBld.append(o.getClass().getSimpleName());

		strBld.append(" SET ");
		strBld.append(String.join(", ", iteratorToIterable(fields.stream().map((e) -> {
			return e.getKey() + " = ?";
		}).iterator())));

		strBld.append(" WHERE ");
		strBld.append(String.join(" AND ", iteratorToIterable(fields.stream().map((e) -> {
			return e.getKey() + " = ?";
		}).iterator())));

		return strBld.toString();
	}

	/**
	 * Generates the update table query for the given java bean object.<br/>
	 * Uses {@code DatabaseKey} to identify the primary keys.
	 * 
	 * @see DatabaseKey
	 * @param o
	 *            The bean to create the query from.
	 * @return The update table query for the object given.
	 */
	public String updateByIdTable(Object o) {
		List<String> pk = this.getPrimaryKeys(o);
		if (pk.size() > 0) {
			List<Entry<String, String>> fields = this.getFields(o);
			final StringBuilder strBld = new StringBuilder();

			strBld.append("UPDATE ");
			strBld.append(o.getClass().getSimpleName());

			strBld.append(" SET ");
			strBld.append(String.join(", ", iteratorToIterable(fields.stream().map((e) -> {
				return e.getKey() + " = ?";
			}).iterator())));

			strBld.append(" WHERE ");
			strBld.append(String.join(" AND ", iteratorToIterable(pk.stream().map((e) -> {
				return e + " = ?";
			}).iterator())));

			return strBld.toString();
		} else {
			return this.updateTable(o);
		}
	}

	/**
	 * Generates the delete table query for the given java bean object.<br/>
	 * Uses {@code DatabaseKey} to identify the primary keys.
	 * 
	 * @see DatabaseKey
	 * @param o
	 *            The bean to create the query from.
	 * @return The delete table query for the object given.
	 */
	public String deleteByIdTable(Object o) {
		List<String> pk = this.getPrimaryKeys(o);
		if (pk.size() > 0) {
			final StringBuilder strBld = new StringBuilder();
			strBld.append("DELETE FROM ");
			strBld.append(o.getClass().getSimpleName());
			strBld.append(" WHERE ");

			strBld.append(String.join(" AND ", iteratorToIterable(pk.stream().map((e) -> {
				return e + " = ?";
			}).iterator())));

			return strBld.toString();
		} else {
			return this.deleteTable(o);
		}
	}

	/**
	 * Generates the select table query for the given java bean object.<br/>
	 * Uses {@code DatabaseKey} to identify the primary keys.
	 * 
	 * @see DatabaseKey
	 * @param o
	 *            The bean to create the query from.
	 * @return The select table query for the object given.
	 */
	public String selectByIdTable(Object o) {
		List<String> pk = this.getPrimaryKeys(o);
		if (pk.size() > 0) {
			final StringBuilder strBld = new StringBuilder();

			strBld.append("SELECT * FROM ");
			strBld.append(o.getClass().getSimpleName());
			strBld.append(" WHERE ");

			strBld.append(String.join(" AND ", iteratorToIterable(pk.stream().map((e) -> {
				return e + " = ?";
			}).iterator())));

			return strBld.toString();
		} else {
			return this.selectTable(o);
		}
	}
	/**
	 * Generates the select all table query for the given java bean object.<br/>
	 * 
	 * @param o
	 *            The bean to create the query from.
	 * @return The select table query for the object given.
	 */
	public String selectAllTable(Object o) {
		final StringBuilder strBld = new StringBuilder();

		strBld.append("SELECT * FROM ");
		strBld.append(o.getClass().getSimpleName());

		return strBld.toString();
	}

	/**
	 * Generates the drop table query for the given java bean object.<br/>
	 * 
	 * @param o
	 *            The bean to create the query from.
	 * @return The drop table query for the object given.
	 */
	public String dropTable(Object o) {
		final StringBuilder strBld = new StringBuilder();

		strBld.append("DROP TABLE ");
		strBld.append(o.getClass().getSimpleName());

		return strBld.toString();
	}

}
