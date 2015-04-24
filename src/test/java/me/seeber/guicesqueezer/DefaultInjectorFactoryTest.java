/*
 * #%L
 * Guice Squeezer test factory for Guice and JUnit
 * %%
 * Copyright (C) 2015 Jochen Seeber
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package me.seeber.guicesqueezer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class DefaultInjectorFactoryTest {
    
    private DefaultInjectorFactory factory;
    
    @Before
    public void initializeTest() {
        this.factory = new DefaultInjectorFactory();
    }
    
    @Test
    public void testCreateModuleFromClass_TopLevel() throws InitializationError {
        this.factory.createModuleFromClass(TestModule.class);
    }
    
    public static class TestCreateModuleFromClass_Nested {
        
        public static class NestedModule extends TestModule {
        }
        
    }
    
    @Test
    public void testCreateModuleFromClass_Nested() throws InitializationError {
        this.factory.createModuleFromClass(TestCreateModuleFromClass_Nested.NestedModule.class);
    }
    
    public static class TestCreateModuleFromClass_FailsIfNoDefaultConstructor {
        
        public static class NestedModule extends TestModule {
            public NestedModule(String name) {
                super(name);
            }
        }
        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleFromClass_FailsIfNoDefaultConstructor() throws InitializationError {
        this.factory.createModuleFromClass(TestCreateModuleFromClass_FailsIfNoDefaultConstructor.NestedModule.class);
    }
    
    public static class TestCreateModuleFromClass_FailsIfNotPublic {
        
        protected static class NestedModule extends TestModule {
        }
        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleFromClass_FailsIfNotPublic() throws InitializationError {
        this.factory.createModuleFromClass(TestCreateModuleFromClass_FailsIfNotPublic.NestedModule.class);
    }
    
    public static class TestCreateModuleFromClass_FailsIfNotStatic {
        
        public class NestedModule extends TestModule {
        }
        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleFromClass_FailsIfNotStatic() throws InitializationError {
        this.factory.createModuleFromClass(TestCreateModuleFromClass_FailsIfNotStatic.NestedModule.class);
    }
    
    public static class TestCreateModuleFromClass_FailsIfConstructorThrowsException {
        
        public static class NestedModule extends TestModule {
            public NestedModule() throws InstantiationException {
                throw new InstantiationException("BOOM!");
            }
        }
        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleFromClass_FailsIfConstructorThrowsException() throws InitializationError {
        this.factory
                .createModuleFromClass(TestCreateModuleFromClass_FailsIfConstructorThrowsException.NestedModule.class);
    }
    
    public static class TestCreateModuleFromMethod {
        
        public static Module createModule() {
            return new TestModule();
        }
        
    }
    
    @Test
    public void testCreateModuleFromMethod() throws InitializationError, NoSuchMethodException {
        Method method = TestCreateModuleFromMethod.class.getMethod("createModule");
        Module module = this.factory.createModuleFromMethod(method);
        assertThat(module).isNotNull();
    }
    
    public static class TestCreateModuleFromMethod_FailsIfNotStatic {
        
        public Module createModule() {
            return new TestModule();
        }
        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleFromMethod_FailsIfNotStatic() throws InitializationError, NoSuchMethodException {
        Method method = TestCreateModuleFromMethod_FailsIfNotStatic.class.getMethod("createModule");
        this.factory.createModuleFromMethod(method);
    }
    
    public static class TestCreateModuleFromMethod_FailsIfNotPublic {
        
        protected static Module createModule() {
            return new TestModule();
        }
        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleFromMethod_FailsIfNotPublic() throws InitializationError, NoSuchMethodException {
        for (Method method : TestCreateModuleFromMethod_FailsIfNotPublic.class.getDeclaredMethods()) {
            if (method.getName().equals("createModule")) {
                assertThat(method.getModifiers() & Modifier.PROTECTED).as("Protected").isNotZero();
                
                this.factory.createModuleFromMethod(method);
            }
        }
        
        fail("Test method 'nonPublicCreateModule' not found");
    }
    
    public static class TestCreateModuleFromMethod_FailsIfNotReturningModule {
        
        public static String createModule() {
            return ":-]";
        }
        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleFromMethod_FailsIfNotReturningModule() throws InitializationError,
            NoSuchMethodException {
        Method method = TestCreateModuleFromMethod_FailsIfNotReturningModule.class.getMethod("createModule");
        this.factory.createModuleFromMethod(method);
    }
    
    public static class TestCreateModuleFromMethod_FailsIfMethodThrowsException {
        
        public static Module createModule() {
            throw new IllegalArgumentException("BOOM!");
        }
        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleFromMethod_FailsIfMethodThrowsException() throws InitializationError,
            NoSuchMethodException {
        Method method = TestCreateModuleFromMethod_FailsIfMethodThrowsException.class.getMethod("createModule");
        this.factory.createModuleFromMethod(method);
    }
    
    public static class TestCreateModuleUsingBoundNestedClasses {
        
        public interface TestInterface1 {
        }
        
        @Bind
        public static class TestClass1 implements TestInterface1 {
        }
        
        public interface TestInterface2 {
        }
        
        @Bind(TestInterface2.class)
        public static class TestClass2 implements Cloneable, TestInterface2 {
        }
        
        public static Module createModule() {
            throw new IllegalArgumentException("BOOM!");
        }
        
        public static class TestClass3 {
        }
        
        @Bind
        public static class SpecialTestClass3 extends TestClass3 {
        }
        
        public static class TestClass4 {
        }
        
        public static class SpecialTestClass4 extends TestClass4 {
        }
        
        @Bind(TestClass4.class)
        public static class MoreSpecialTestClass4 extends SpecialTestClass4 {
        }
    }
    
    @Test
    public void testCreateModuleUsingBoundNestedClasses() {
        Module module = this.factory.createModuleUsingBoundNestedClasses(TestCreateModuleUsingBoundNestedClasses.class);
        Injector injector = Guice.createInjector(module);
        
        assertThat(injector.getInstance(TestCreateModuleUsingBoundNestedClasses.TestInterface1.class)).isInstanceOf(
                TestCreateModuleUsingBoundNestedClasses.TestClass1.class);
        assertThat(injector.getInstance(TestCreateModuleUsingBoundNestedClasses.TestInterface2.class)).isInstanceOf(
                TestCreateModuleUsingBoundNestedClasses.TestClass2.class);
        assertThat(injector.getInstance(TestCreateModuleUsingBoundNestedClasses.TestClass3.class)).isInstanceOf(
                TestCreateModuleUsingBoundNestedClasses.SpecialTestClass3.class);
        assertThat(injector.getInstance(TestCreateModuleUsingBoundNestedClasses.TestClass4.class)).isInstanceOf(
                TestCreateModuleUsingBoundNestedClasses.MoreSpecialTestClass4.class);
    }
    
    public static class NonStaticBindingTest {
        
        @Bind
        public class TestClass {
        }
        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleUsingBoundNestedClasses_FailsIfNotStatic() {
        this.factory.createModuleUsingBoundNestedClasses(NonStaticBindingTest.class);
    }
    
    public static class BindingWithoutSuperclassOrInterfaceTest {
        
        @Bind
        public static class TestClass {
        }
        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleUsingBoundNestedClasses_FailsIfNoInterfaceOrSuperclass() {
        this.factory.createModuleUsingBoundNestedClasses(BindingWithoutSuperclassOrInterfaceTest.class);
    }
    
    public static class TestCaseWithNestedModule {
        
        public static class NonStaticModule extends TestModule {
        }
        
    }
    
    @Test
    public void testCreateModuleUsingNestedModuleClasses() {
        Module module = this.factory.createModuleUsingNestedModuleClasses(TestCaseWithNestedModule.class);
        Injector injector = Guice.createInjector(module);
        assertThat(injector.getInstance(Key.get(String.class, Names.named("default")))).isEqualTo("1");
    }
    
    public static class TestCaseWithNonStaticNestedModule {
        
        public class NonStaticModule extends TestModule {
        }
        
    }
    
    @Test
    public void testCreateModuleUsingNestedModuleClasses_IgnoreNonStatic() {
        Module module = this.factory.createModuleUsingNestedModuleClasses(TestCaseWithNonStaticNestedModule.class);
        Injector injector = Guice.createInjector(module);
        assertThat(injector.getAllBindings().get(Key.get(String.class, Names.named("default")))).isNull();
    }
    
    public static class TestCaseWithNonModuleNestedClass {
        
        public class NestedClass extends TestModule {
        }
        
    }
    
    @Test
    public void testCreateModuleUsingNestedModuleClasses_IgnoreNonModule() {
        Module module = this.factory.createModuleUsingNestedModuleClasses(TestCaseWithNonModuleNestedClass.class);
        Injector injector = Guice.createInjector(module);
        assertThat(injector.getAllBindings().get(Key.get(String.class, Names.named("default")))).isNull();
    }
    
    public static class TestCreateModuleUsingFactoryMethods {
        
        public static Module createModule() {
            return new TestModule("method");
        }
        
    }
    
    @Test
    public void testCreateModuleUsingFactoryMethods() {
        Module module = this.factory.createModuleUsingFactoryMethods(TestCreateModuleUsingFactoryMethods.class);
        Injector injector = Guice.createInjector(module);
        assertThat(injector.getInstance(Key.get(String.class, Names.named("method")))).isEqualTo("1");
    }
    
    public static class TestCaseWithNonStaticFactoryMethod {
        
        public Module createModule() {
            return new TestModule();
        }
        
    }
    
    @Test
    public void testCreateModuleUsingFactoryMethods_IgnoreNonStatic() {
        Module module = this.factory.createModuleUsingFactoryMethods(TestCaseWithNonStaticFactoryMethod.class);
        Injector injector = Guice.createInjector(module);
        assertThat(injector.getAllBindings().get(Key.get(String.class, Names.named("default")))).isNull();
    }
    
    public static class TestCaseWithFactoryMethodWithParameter {
        
        public static Module createModule(String name) {
            return new TestModule();
        }
        
    }
    
    @Test
    public void testCreateModuleUsingFactoryMethods_IgnoreWithParameters() {
        Module module = this.factory.createModuleUsingFactoryMethods(TestCaseWithFactoryMethodWithParameter.class);
        Injector injector = Guice.createInjector(module);
        assertThat(injector.getAllBindings().get(Key.get(String.class, Names.named("default")))).isNull();
    }
    
    public static class TestCaseWithNonModuleFactoryMethod {
        
        protected static String createString() {
            return "Hello";
        }
        
    }
    
    @Test
    public void testCreateModuleUsingFactoryMethods_IgnoreNonModuleReturn() {
        Module module = this.factory.createModuleUsingFactoryMethods(TestCaseWithNonModuleFactoryMethod.class);
        Injector injector = Guice.createInjector(module);
        assertThat(injector.getAllBindings().get(Key.get(String.class, Names.named("default")))).isNull();
    }
    
    public static class TestValidateMethods_WithParameterWithQualifier {
        
        public void testMethod(@Named("default") String name) {
        }
        
    }
    
    @Test
    public void testValidateMethods_WithParameterWithQualifier() throws NoSuchMethodException, SecurityException {
        FrameworkMethod method = new FrameworkMethod(TestValidateMethods_WithParameterWithQualifier.class.getMethod(
                "testMethod", String.class));
        List<Throwable> errors = this.factory.validateTestMethods(Collections.singletonList(method));
        assertThat(errors).isEmpty();
    }
    
    public static class TestValidateMethods_WithParameterWithBindingAnnotation {
        
        public void testMethod(@com.google.inject.name.Named("default") String name) {
        }
        
    }
    
    @Test
    public void testValidateMethods_WithParameterWithBindingAnnotation() throws NoSuchMethodException,
            SecurityException {
        FrameworkMethod method = new FrameworkMethod(
                TestValidateMethods_WithParameterWithBindingAnnotation.class.getMethod("testMethod", String.class));
        List<Throwable> errors = this.factory.validateTestMethods(Collections.singletonList(method));
        assertThat(errors).isEmpty();
    }
    
    public static class TestValidateMethods_WithParameterWithOtherAnnotation {
        
        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface OtherAnnotation {
        }
        
        public void testMethod(@OtherAnnotation String name) {
        }
        
    }
    
    @Test
    public void testValidateMethods_WithParameterWithOtherAnnotation() throws NoSuchMethodException, SecurityException {
        FrameworkMethod method = new FrameworkMethod(
                TestValidateMethods_WithParameterWithOtherAnnotation.class.getMethod("testMethod", String.class));
        List<Throwable> errors = this.factory.validateTestMethods(Collections.singletonList(method));
        assertThat(errors).isEmpty();
    }
    
    public static class TestValidateMethods_FailsIfParameterHasMultipleQualifiers {
        
        public void testMethod(@Named("default") @com.google.inject.name.Named("default") String name) {
        }
        
    }
    
    @Test
    public void testValidateMethods_FailsIfParameterHasMultipleQualifiers() throws NoSuchMethodException,
            SecurityException {
        FrameworkMethod method = new FrameworkMethod(
                TestValidateMethods_FailsIfParameterHasMultipleQualifiers.class.getMethod("testMethod", String.class));
        List<Throwable> errors = this.factory.validateTestMethods(Collections.singletonList(method));
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).isInstanceOf(IllegalArgumentException.class);
        
    }
    
    @TestModules(TestModule.class)
    public static class TestCreateTestClassInjector {
        
        public static class NestedModule extends AbstractModule {
            @Override
            protected void configure() {
                bind(String.class).annotatedWith(Names.named("nested")).toInstance("1");
            }
        }
        
        public interface TestInterface1 {
        }
        
        @Bind
        public static class TestClass1 implements TestInterface1 {
        }
        
        public interface TestInterface2 {
        }
        
        @Bind(TestInterface2.class)
        public static class TestClass2 implements Cloneable, TestInterface2 {
        }
        
        public static class TestClass3 {
        }
        
        @Bind(TestClass3.class)
        public static class SubclassedTestClass3 extends TestClass3 {
        }
        
        public static class TestClass4 {
        }
        
        @Bind
        public static class SubclassedTestClass4 extends TestClass4 {
        }
        
        public static Module createModule() {
            return new TestModule("method");
        }
        
        @TestModules(AlternateTestModule.class)
        public void testMethod(@Named("alternate") String name) {
        }
    }
    
    @Test
    public void testCreateTestClassInjector() throws InitializationError {
        Injector injector = this.factory.createTestClassInjector(TestCreateTestClassInjector.class);
        assertThat(injector.getInstance(Key.get(String.class, Names.named("default")))).isEqualTo("1");
        assertThat(injector.getInstance(Key.get(String.class, Names.named("nested")))).isEqualTo("1");
        assertThat(injector.getInstance(Key.get(String.class, Names.named("method")))).isEqualTo("1");
        assertThat(injector.getInstance(TestCreateTestClassInjector.TestInterface1.class)).isInstanceOf(
                TestCreateTestClassInjector.TestClass1.class);
        assertThat(injector.getInstance(TestCreateTestClassInjector.TestInterface2.class)).isInstanceOf(
                TestCreateTestClassInjector.TestClass2.class);
    }
    
    @TestModules(TestModule.class)
    public static class TestCreateTestMethodInjector {
        
        public void testMethod(String name) {
        }
        
    }
    
    @Test
    public void testCreateTestMethodInjector() throws InitializationError, NoSuchMethodException, SecurityException {
        Injector classInjector = this.factory.createTestClassInjector(TestCreateTestMethodInjector.class);
        Injector methodInjector = this.factory.createTestMethodInjector(
                TestCreateTestMethodInjector.class.getMethod("testMethod", String.class), classInjector);
        
        assertThat(methodInjector.getInstance(Key.get(String.class, Names.named("default")))).isEqualTo("1");
    }
    
    @TestModules(TestModule.class)
    public static class TestCreateTestMethodInjector_WithAnnotation {
        
        @TestModules(AlternateTestModule.class)
        public void testMethod(String name) {
        }
        
    }
    
    @Test
    public void testCreateTestMethodInjector_WithAnnotation() throws InitializationError, NoSuchMethodException,
            SecurityException {
        Injector classInjector = this.factory
                .createTestClassInjector(TestCreateTestMethodInjector_WithAnnotation.class);
        Injector methodInjector = this.factory.createTestMethodInjector(
                TestCreateTestMethodInjector_WithAnnotation.class.getMethod("testMethod", String.class), classInjector);
        
        assertThat(methodInjector.getInstance(Key.get(String.class, Names.named("alternate")))).isEqualTo("1");
    }
    
    @TestModules(TestModule.class)
    public static class TestCreateInvocationStatement {
        public void testMethod() {
        }
    }
    
    @Test
    public void testCreateInvocationStatement() throws InitializationError, NoSuchMethodException, SecurityException {
        Injector injector = this.factory.createTestClassInjector(TestCreateInvocationStatement.class);
        TestCreateInvocationStatement testObject = new TestCreateInvocationStatement();
        
        FrameworkMethod testMethod = new FrameworkMethod(TestCreateInvocationStatement.class.getMethod("testMethod"));
        Statement statement = this.factory.createInvocationStatement(testMethod, testObject, injector);
        InvokeMethod expectedStatement = new InvokeMethod(testMethod, testObject);
        
        assertThat(statement).isInstanceOf(InvokeMethod.class);
        assertThat(statement).isEqualToComparingFieldByField(expectedStatement);
    }
    
    @TestModules(TestModule.class)
    public static class TestCreateInvocationStatement_WithParameter {
        public void testMethod(String name) {
        }
        
    }
    
    @Test
    public void testCreateInvocationStatement_WithParameter() throws InitializationError, NoSuchMethodException,
            SecurityException {
        Injector injector = this.factory.createTestClassInjector(TestCreateInvocationStatement_WithParameter.class);
        TestCreateInvocationStatement_WithParameter testObject = new TestCreateInvocationStatement_WithParameter();
        
        FrameworkMethod testMethod = new FrameworkMethod(TestCreateInvocationStatement_WithParameter.class.getMethod(
                "testMethod", String.class));
        Statement statement = this.factory.createInvocationStatement(testMethod, testObject, injector);
        InvokeMethodWithParameters expectedStatement = new InvokeMethodWithParameters(testMethod, testObject, "");
        
        assertThat(statement).isInstanceOf(InvokeMethodWithParameters.class);
        assertThat(statement).isEqualToComparingFieldByField(expectedStatement);
    }
    
    @TestModules(TestModule.class)
    public static class TestCreateInvocationStatement_WithParameterWithQualifier {
        public void testMethod(@Named("default") String name) {
        }
        
    }
    
    @Test
    public void testCreateInvocationStatement_WithParameterWithQualifier() throws InitializationError,
            NoSuchMethodException, SecurityException {
        Injector injector = this.factory
                .createTestClassInjector(TestCreateInvocationStatement_WithParameterWithQualifier.class);
        TestCreateInvocationStatement_WithParameterWithQualifier testObject = new TestCreateInvocationStatement_WithParameterWithQualifier();
        
        FrameworkMethod testMethod = new FrameworkMethod(
                TestCreateInvocationStatement_WithParameterWithQualifier.class.getMethod("testMethod", String.class));
        Statement statement = this.factory.createInvocationStatement(testMethod, testObject, injector);
        InvokeMethodWithParameters expectedStatement = new InvokeMethodWithParameters(testMethod, testObject, "1");
        
        assertThat(statement).isInstanceOf(InvokeMethodWithParameters.class);
        assertThat(statement).isEqualToComparingFieldByField(expectedStatement);
    }
    
    @TestModules(TestModule.class)
    public static class testCreateInvocationStatement_WithParameterWithBindingAnnotation {
        public void testMethod(@com.google.inject.name.Named("default") String name) {
        }
        
    }
    
    @Test
    public void testCreateInvocationStatement_WithParameterWithBindingAnnotation() throws InitializationError,
            NoSuchMethodException, SecurityException {
        Injector injector = this.factory.createTestClassInjector(TestCreateInvocationStatement.class);
        testCreateInvocationStatement_WithParameterWithBindingAnnotation testObject = new testCreateInvocationStatement_WithParameterWithBindingAnnotation();
        
        FrameworkMethod testMethod = new FrameworkMethod(
                testCreateInvocationStatement_WithParameterWithBindingAnnotation.class.getMethod("testMethod",
                        String.class));
        Statement statement = this.factory.createInvocationStatement(testMethod, testObject, injector);
        InvokeMethodWithParameters expectedStatement = new InvokeMethodWithParameters(testMethod, testObject, "1");
        
        assertThat(statement).isInstanceOf(InvokeMethodWithParameters.class);
        assertThat(statement).isEqualToComparingFieldByField(expectedStatement);
    }
    
    @TestModules(TestModule.class)
    public static class TestCreateInvocationStatement_WithParameterWithOtherStatement {
        
        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface OtherAnnotation {
        }
        
        public void testMethod(@OtherAnnotation String name) {
        }
        
    }
    
    @Test
    public void testCreateInvocationStatement_WithParameterWithOtherStatement() throws InitializationError,
            NoSuchMethodException, SecurityException {
        Injector injector = this.factory
                .createTestClassInjector(TestCreateInvocationStatement_WithParameterWithOtherStatement.class);
        TestCreateInvocationStatement_WithParameterWithOtherStatement testObject = new TestCreateInvocationStatement_WithParameterWithOtherStatement();
        
        FrameworkMethod testMethod = new FrameworkMethod(
                TestCreateInvocationStatement_WithParameterWithOtherStatement.class.getMethod("testMethod",
                        String.class));
        Statement statement = this.factory.createInvocationStatement(testMethod, testObject, injector);
        InvokeMethodWithParameters expectedStatement = new InvokeMethodWithParameters(testMethod, testObject, "");
        
        assertThat(statement).isInstanceOf(InvokeMethodWithParameters.class);
        assertThat(statement).isEqualToComparingFieldByField(expectedStatement);
    }
    
}
