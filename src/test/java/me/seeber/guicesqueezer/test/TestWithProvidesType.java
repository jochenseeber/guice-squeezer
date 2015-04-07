
package me.seeber.guicesqueezer.test;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import me.seeber.guicesqueezer.GuiceSqueezer;
import me.seeber.guicesqueezer.ProvidesType;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GuiceSqueezer.class)
public class TestWithProvidesType {
    
    public interface TestInterface1 {
    }
    
    @ProvidesType
    public static class TestClass1 implements TestInterface1 {
    }
    
    public interface TestInterface2 {
    }
    
    @ProvidesType(TestInterface2.class)
    public static class TestClass2 implements Cloneable, TestInterface2 {
    }
    
    @Inject
    private TestInterface1 test1;
    
    @Inject
    private TestInterface2 test2;
    
    @Test
    public void testProvidesType() {
        assertThat(this.test1).isInstanceOf(TestClass1.class);
    }
    
    @Test
    public void testProvidesTypeWithType() {
        assertThat(this.test2).isInstanceOf(TestClass2.class);
    }
}
