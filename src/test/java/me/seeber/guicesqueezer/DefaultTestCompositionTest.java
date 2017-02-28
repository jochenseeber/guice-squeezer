/**
 * BSD 2-Clause License
 *
 * Copyright (c) 2016-2017, Jochen Seeber
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;

import me.seeber.guicesqueezer.java.Validate;

@SuppressWarnings("javadoc")
public class DefaultTestCompositionTest {

    @Nullable
    private DefaultTestComposition factory;

    @Before
    public void initializeTest() {
        this.factory = new DefaultTestComposition();
    }

    @Test
    public void testCreateModuleFromClass_TopLevel() {
        getFactory().createModuleFromClass(TestModule.class);
    }

    public static class TestCreateModuleFromClass_Nested {

        public static class NestedModule extends TestModule {
        }

    }

    @Test
    public void testCreateModuleFromClass_Nested() {
        getFactory().createModuleFromClass(TestCreateModuleFromClass_Nested.NestedModule.class);
    }

    public static class TestCreateModuleFromClass_FailsIfNoDefaultConstructor extends TestModule {

        public static class NestedModule extends TestModule {

            public NestedModule(String name) {
                super(name);
            }

        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleFromClass_FailsIfNoDefaultConstructor() {
        getFactory().createModuleFromClass(TestCreateModuleFromClass_FailsIfNoDefaultConstructor.NestedModule.class);
    }

    public static class TestCreateModuleFromClass_FailsIfNotPublic {

        protected static class NestedModule extends TestModule {
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleFromClass_FailsIfNotPublic() {
        getFactory().createModuleFromClass(TestCreateModuleFromClass_FailsIfNotPublic.NestedModule.class);
    }

    public static class TestCreateModuleFromClass_FailsIfNotStatic {

        public class NestedModule extends TestModule {
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleFromClass_FailsIfNotStatic() {
        getFactory().createModuleFromClass(TestCreateModuleFromClass_FailsIfNotStatic.NestedModule.class);
    }

    public static class TestCreateModuleFromClass_FailsIfConstructorThrowsException {

        public static class NestedModule extends TestModule {

            public NestedModule() throws InstantiationException {
                throw new InstantiationException("BOOM!");
            }
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleFromClass_FailsIfConstructorThrowsException() {
        getFactory()
                .createModuleFromClass(TestCreateModuleFromClass_FailsIfConstructorThrowsException.NestedModule.class);
    }

    public static class TestCreateModuleFromMethod {

        public static Module createModule() {
            return new TestModule();
        }

    }

    @Test
    public void testCreateModuleFromMethod() throws NoSuchMethodException {
        Method method = TestCreateModuleFromMethod.class.getMethod("createModule");
        Module module = getFactory().createModuleFromMethod(method);
        assertThat(module).isNotNull();
    }

    public static class TestCreateModuleFromMethod_FailsIfNotStatic {

        public Module createModule() {
            return new TestModule();
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleFromMethod_FailsIfNotStatic() throws NoSuchMethodException {
        Method method = TestCreateModuleFromMethod_FailsIfNotStatic.class.getMethod("createModule");
        getFactory().createModuleFromMethod(method);
    }

    public static class TestCreateModuleFromMethod_FailsIfNotPublic {

        protected static Module createModule() {
            return new TestModule();
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleFromMethod_FailsIfNotPublic() {

        for (Method method : TestCreateModuleFromMethod_FailsIfNotPublic.class.getDeclaredMethods()) {
            if (method.getName().equals("createModule")) {
                assertThat(method.getModifiers() & Modifier.PROTECTED).as("protected").isEqualTo(Modifier.PROTECTED);
                getFactory().createModuleFromMethod(method);
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
    public void testCreateModuleFromMethod_FailsIfNotReturningModule() throws NoSuchMethodException {
        Method method = TestCreateModuleFromMethod_FailsIfNotReturningModule.class.getMethod("createModule");
        getFactory().createModuleFromMethod(method);
    }

    public static class TestCreateModuleFromMethod_FailsIfMethodThrowsException {

        public static Module createModule() {
            throw new IllegalArgumentException("BOOM!");
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleFromMethod_FailsIfMethodThrowsException() throws NoSuchMethodException {
        Method method = TestCreateModuleFromMethod_FailsIfMethodThrowsException.class.getMethod("createModule");
        getFactory().createModuleFromMethod(method);
    }

    /**
     * Test classes for {@link #testCreateModuleFromMethod}
     */
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
        Module module = getFactory().createModuleUsingBoundNestedClasses(TestCreateModuleUsingBoundNestedClasses.class);
        Injector injector = Guice.createInjector(module);

        assertThat(injector.getInstance(TestCreateModuleUsingBoundNestedClasses.TestInterface1.class))
                .isInstanceOf(TestCreateModuleUsingBoundNestedClasses.TestClass1.class);
        assertThat(injector.getInstance(TestCreateModuleUsingBoundNestedClasses.TestInterface2.class))
                .isInstanceOf(TestCreateModuleUsingBoundNestedClasses.TestClass2.class);
        assertThat(injector.getInstance(TestCreateModuleUsingBoundNestedClasses.TestClass3.class))
                .isInstanceOf(TestCreateModuleUsingBoundNestedClasses.SpecialTestClass3.class);
        assertThat(injector.getInstance(TestCreateModuleUsingBoundNestedClasses.TestClass4.class))
                .isInstanceOf(TestCreateModuleUsingBoundNestedClasses.MoreSpecialTestClass4.class);
    }

    public static class NonStaticBindingTest {

        @Bind
        public class TestClass {
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleUsingBoundNestedClasses_FailsIfNotStatic() {
        getFactory().createModuleUsingBoundNestedClasses(NonStaticBindingTest.class);
    }

    public static class BindingWithoutSuperclassOrInterfaceTest {

        @Bind
        public static class TestClass {
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateModuleUsingBoundNestedClasses_FailsIfNoInterfaceOrSuperclass() {
        getFactory().createModuleUsingBoundNestedClasses(BindingWithoutSuperclassOrInterfaceTest.class);
    }

    public static class TestCaseWithNestedModule {

        public static class NonStaticModule extends TestModule {
        }

    }

    @Test
    public void testCreateModuleUsingNestedModuleClasses() {
        Module module = getFactory().createModuleUsingNestedModuleClasses(TestCaseWithNestedModule.class);
        Injector injector = Guice.createInjector(module);
        assertThat(injector.getInstance(Key.get(String.class, Names.named("default")))).isEqualTo("1");
    }

    /**
     * Test classes for {@link #testCreateModuleFromMethod}
     */
    public static class TestCaseWithNonStaticNestedModule {

        public class NonStaticModule extends TestModule {
        }

    }

    @Test
    public void testCreateModuleUsingNestedModuleClasses_IgnoreNonStatic() {
        Module module = getFactory().createModuleUsingNestedModuleClasses(TestCaseWithNonStaticNestedModule.class);
        Injector injector = Guice.createInjector(module);
        assertThat(injector.getAllBindings().get(Key.get(String.class, Names.named("default")))).isNull();
    }

    public static class TestCaseWithNonModuleNestedClass {

        public class NestedClass extends TestModule {
        }

    }

    @Test
    public void testCreateModuleUsingNestedModuleClasses_IgnoreNonModule() {
        Module module = getFactory().createModuleUsingNestedModuleClasses(TestCaseWithNonModuleNestedClass.class);
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
        Module module = getFactory().createModuleUsingFactoryMethods(TestCreateModuleUsingFactoryMethods.class);
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
        Module module = getFactory().createModuleUsingFactoryMethods(TestCaseWithNonStaticFactoryMethod.class);
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
        Module module = getFactory().createModuleUsingFactoryMethods(TestCaseWithFactoryMethodWithParameter.class);
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
        Module module = getFactory().createModuleUsingFactoryMethods(TestCaseWithNonModuleFactoryMethod.class);
        Injector injector = Guice.createInjector(module);
        assertThat(injector.getAllBindings().get(Key.get(String.class, Names.named("default")))).isNull();
    }

    public static class TestValidateMethods_WithParameterWithQualifier {

        public void testMethod(@Named("default") String name) {
        }

    }

    @Test
    public void testValidateMethods_WithParameterWithQualifier() throws NoSuchMethodException, SecurityException {
        FrameworkMethod method = new FrameworkMethod(
                TestValidateMethods_WithParameterWithQualifier.class.getMethod("testMethod", String.class));
        List<Throwable> errors = getFactory().validateTestMethods(Collections.singletonList(method));
        assertThat(errors).isEmpty();
    }

    public static class TestValidateMethods_WithParameterWithBindingAnnotation {

        public void testMethod(@com.google.inject.name.Named("default") String name) {
        }

    }

    @Test
    public void testValidateMethods_WithParameterWithBindingAnnotation()
            throws NoSuchMethodException, SecurityException {
        FrameworkMethod method = new FrameworkMethod(
                TestValidateMethods_WithParameterWithBindingAnnotation.class.getMethod("testMethod", String.class));
        List<Throwable> errors = getFactory().validateTestMethods(Collections.singletonList(method));
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
        List<Throwable> errors = getFactory().validateTestMethods(Collections.singletonList(method));
        assertThat(errors).isEmpty();
    }

    public static class TestValidateMethods_FailsIfParameterHasMultipleQualifiers {

        public void testMethod(@Named("default") @com.google.inject.name.Named("default") String name) {
        }

    }

    @Test
    public void testValidateMethods_FailsIfParameterHasMultipleQualifiers()
            throws NoSuchMethodException, SecurityException {
        FrameworkMethod method = new FrameworkMethod(
                TestValidateMethods_FailsIfParameterHasMultipleQualifiers.class.getMethod("testMethod", String.class));
        List<Throwable> errors = getFactory().validateTestMethods(Collections.singletonList(method));
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
    public void testCreateTestClassInjector() {
        Module module = getFactory().createTestClassModule(TestCreateTestClassInjector.class);
        Injector injector = Guice.createInjector(module);
        assertThat(injector.getInstance(Key.get(String.class, Names.named("default")))).isEqualTo("1");
        assertThat(injector.getInstance(Key.get(String.class, Names.named("nested")))).isEqualTo("1");
        assertThat(injector.getInstance(Key.get(String.class, Names.named("method")))).isEqualTo("1");
        assertThat(injector.getInstance(TestCreateTestClassInjector.TestInterface1.class))
                .isInstanceOf(TestCreateTestClassInjector.TestClass1.class);
        assertThat(injector.getInstance(TestCreateTestClassInjector.TestInterface2.class))
                .isInstanceOf(TestCreateTestClassInjector.TestClass2.class);
    }

    @TestModules(TestModule.class)
    public static class TestCreateTestMethodInjector {

        public void testMethod(String name) {
        }

    }

    @Test
    public void testCreateTestMethodInjector() throws NoSuchMethodException, SecurityException {
        Module classModule = getFactory().createTestClassModule(TestCreateTestClassInjector.class);
        Module methodModule = getFactory()
                .createTestMethodModule(TestCreateTestMethodInjector.class.getMethod("testMethod", String.class));
        Module module = Modules.override(classModule).with(methodModule);
        Injector injector = Guice.createInjector(module);

        assertThat(injector.getInstance(Key.get(String.class, Names.named("default")))).isEqualTo("1");
    }

    @TestModules(TestModule.class)
    public static class TestCreateTestMethodInjector_WithAnnotation {

        @TestModules(AlternateTestModule.class)
        public void testMethod(String name) {
        }

    }

    @Test
    public void testCreateTestMethodInjector_WithAnnotation() throws NoSuchMethodException, SecurityException {
        Module classModule = getFactory().createTestClassModule(TestCreateTestMethodInjector_WithAnnotation.class);
        Module methodModule = getFactory().createTestMethodModule(
                TestCreateTestMethodInjector_WithAnnotation.class.getMethod("testMethod", String.class));
        Module module = Modules.override(classModule).with(methodModule);
        Injector injector = Guice.createInjector(module);

        assertThat(injector.getInstance(Key.get(String.class, Names.named("alternate")))).isEqualTo("1");
    }

    @TestModules(TestModule.class)
    public static class TestCreateInvocationStatement {
        public void testMethod() {
        }
    }

    @Test
    public void testCreateInvocationStatement() throws NoSuchMethodException, SecurityException {
        Module classModule = getFactory().createTestClassModule(TestCreateInvocationStatement.class);
        Injector injector = Guice.createInjector(classModule);
        TestCreateInvocationStatement testObject = new TestCreateInvocationStatement();

        FrameworkMethod testMethod = new FrameworkMethod(TestCreateInvocationStatement.class.getMethod("testMethod"));
        Statement statement = getFactory().createInvocationStatement(testMethod, testObject, injector);

        assertThat(statement).isInstanceOf(InvokeWithParametersStatement.class);
    }

    public DefaultTestComposition getFactory() {
        return Validate.notNull(this.factory, "factory");
    }

}
