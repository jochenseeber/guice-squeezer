Guice Squeezer Test Runner
==========================

Guice Squeezer is a JUnit test runner to run Guice based unit tests with as little boilerplate code as possible. It's goal is to let you create unit tests with ease and without any distracting setup code.

Usage
-----

Just add the Guice Squeezer library to your project and tell JUnit to run its tests with GuiceSqueezer. You then can add Guice modules to your tests, and have Guice inject the components for your unit tests, for example:

```java
@RunWith(GuiceSqueezer.class)
@TestModules(MyGuiceModule.class)
public class SimpleTest {
    @Test
    public void testHello(String hello) {
        assertThat(hello).as("hello").isEqualTo("hello");
    }
}
```

Guice Squeezer automatically instantiates the modules specified by `@TestModules` and makes their components available for injection. In addition, there are various other ways to add Guice modules to your tests described below.

Guice Squeezer also supports test methods with parameters. Before calling the test method, the parameter values are resolved using Guice and supplied to the test method. 

Unit tests not only are important to test the correctness of your code, they are also an essential part of documenting how to use your code. By removing the boiler plate, Guice Squeezer lets readers of your unit tests focus on the important parts. For this reason, Guice Squeezer focuses exclusively on the "mechanical" parts of writing Guice based unit tests. It for example does not automatically create mock objects for unbound components on order not to hide any dependencies your code requires from the reader of your unit tests.

### Web Applications

Guice Squeezer also supports testing web application components. Using `GuiceServletSqueezer` as test runner will run each individual test method in a separate request context, allowing you to access request scoped bindings:

```java
@RunWith(GuiceServletSqueezer.class)
public class WebComponentTest {
    public static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class).toProvider(() -> "hello").in(ServletScopes.REQUEST);
        }

    }

    @Test
    public void testInjection(String hello) {
        assertThat(hello).as("hello").isEqualTo("hello");
    }
}
```
 
Configuration
-------------

Guice Squeezer provides several ways to bind components in your unit tests.

### Module Annotations

You can annotate test classes and methods with `@TestModules`. Guice Squeezer will instantiate those modules and provide the bound components to your test:

```java
@RunWith(GuiceSqueezer.class)
@TestModules(MyGuiceModule.class)
public class SimpleTest {
    @Test
    @TestModules(MyOtherGuiceModule.class)
    public void testHello(String hello) {
        assertThat(hello).as("hello").isEqualTo("hello");
    }
}
```
               
The `@TestModules` annotation works at class and method level, so you can specify bindings that are valid for the whole test class, or are valid only for a single test method.

### Nested Module Classes

Guice Squeezer automatically adds any nested static class that implements `Module` to the bindings.

```java
@RunWith(GuiceSqueezer.class)
public class SimpleTest {

    public static TestModule extends AbstractModule {
        protected void configure() {
            bind(String.class).toInstance("hello");
        }
    }

    @Test
    public void testHello(String hello) {
        assertThat(hello).as("hello").isEqualTo("hello");
    }
}
```
    
The nested module class must be public and static.    
    
### Module provider methods

Guice Squeezer automatically calls any public static method that returns a `Module` and adds the result to the bindings:

```java
@RunWith(GuiceSqueezer.class)
public class SimpleTest {

    public static Module testModule() {
        return new AbstractModule() {
            protected void configure() {
                bind(String.class).toInstance("hello");
            }
        }
    }

    @Inject
    private String hello;
    
    @Test
    public void testHello(String hello) {
        assertThat(hello).as("hello").isEqualTo("hello");
    }
}
```

### Bind annotations

Guice Squeezer automatically binds any nested static class that is annotated with `@Bind`:

```java
@RunWith(GuiceSqueezer.class)
public class SimpleTest {
    @Bind(MyInterface.class)
    public static class FirstComponent implements FirstInterface {
        // Binds component to specified interface
    }
    
    @Bind
    public static class SecondComponent implements SecondInterface {
        // Automatically binds the first interface to this class
    }

    @Bind
    public static class ThirdComponent extends AbstractThirdComponent {
        // Automatically binds superclass to this class
    }

    @Test
    public void testHello(FirstInterface first, SecondInterface second, AbstractThirdComponent third) {
        assertThat(first).as("first").isInstanceOf(FirstComponent.class);
        assertThat(second).as("second").isInstanceOf(SecondComponent.class);
        assertThat(third).as("third").isInstanceOf(ThirdComponent.class);
    }
}
```

You can specify the bound interface in the `@Bind` annotation. If you omit this parameter, the class is assumed to be the implementation for the first specified interface. If there are no interfaces, it is assumed to be the implementation for the superclass (unless this superclass is Object).    

Binding and scope annotations are supported, e.g. you can annotate your class with `@Singleton` or `@Named`. 
    
### Provider Methods

Any static method that is annotated with `@Provides` is added to the bindings as provider method (similar to Guice provider methods):

```java
@RunWith(GuiceSqueezer.class)
public class SimpleTest {
    @Provides
    public static String provideString {
        return "1";
    }

    @Test
    public void testInjection(String testString) {
        assertThat(testString).isEqualTo("1");
    }
}
```
    
Methods annotated with `@Provides` must be public and static. Binding and scope annotations are supported, e.g. you can annotate your method with `@Singleton` or `@Named`. 

Injector construction
---------------------

There are four "levels" where bindings can be defined for a test method:

1. Bindings inherited from the test classes superclass
2. Class level modules defined by a `@TestModules` annotation on the test class
3. Modules and bindings defined locally in the test class (e.g. by a nested module class or a `@Provides` method)
4. Method level modules defined by a `@TestModules` annotation on the test method

The final injector used for a test method is created by overriding the bindings from each level with those of the next level using [Modules.override](http://google.github.io/guice/api-docs/latest/javadoc/com/google/inject/util/Modules.html#override-java.lang.Iterable-).

With this, you can e.g. pull in a module using a class level `@TestModules` annotation, and redefine a binding in a lower level, e.g. by using a `@Provides` method in the test class.

Modules and bindings defined locally in the test class (level 3) are combined using [Modules.combine](http://google.github.io/guice/api-docs/latest/javadoc/com/google/inject/util/Modules.html#combine-java.lang.Iterable-) because there is no meaningful priority. One important thing to remember is that Guice prevents you from "redefining" bindings when combining modules, so you cannot define the same binding in a nested module class and a `@Provides` method.

Examples
--------

See the [test](src/test/java/me/seeber/guicesqueezer/test) folder for examples.

License
-------

This plugin is licensed under the [${projectConfig.license.id}](LICENSE.txt) license.
