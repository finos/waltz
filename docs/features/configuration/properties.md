Waltz configuration properties can be defined in:

* `${user.home}/.waltz/waltz.properties` (see waltz.sample.properties for example) or
* JNDI (eg: Tomcat `conf/context.xml`)


JNDI properties take precedence over properties in waltz.properties.
    
And example of a JNDI property defined in Tomcat `conf/context.xml`:

`<Environment name="database.password.jndi"
                  value="myDbPassword"
                  type="java.lang.String"
                  override="false"/>`