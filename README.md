# Beans2SQL
A simple way to translate a java bean object into its own SQL query.

## Example
`Object o = new Persona();`  
`BeanTranslator bt = new BeanTranslator();`  
`bt.createTable(o);`  
`bt.insertTable(o);`  
`bt.deleteTable(o);`  
`bt.selectTable(o);`  
`bt.updateTable(o);`  
`bt.deleteByIdTable(o);`  
`bt.selectByIdTable(o);`  
`bt.updateByIdTable(o);`  
`bt.selectAllTable(o);`  
`bt.dropTable(o);`  
