package gololang.compiler;

import gololang.compiler.parser.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class CompileAndRunTest {

  private static final String SRC = "src/test/resources/for-execution/".replaceAll("/", File.separator);

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private Class<?> load(String goloFile, String moduleClass) throws IOException, ParseException, ClassNotFoundException {
    GoloCompiler compiler = new GoloCompiler();
    compiler.compileTo(goloFile, new FileInputStream(SRC + goloFile), temporaryFolder.getRoot());
    try (URLClassLoader classLoader = new URLClassLoader(new URL[]{temporaryFolder.getRoot().toURI().toURL()})) {
      return classLoader.loadClass(moduleClass);
    }
  }

  @Test
  public void test_functions_with_returns() throws ClassNotFoundException, IOException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> moduleClass = load("returns.golo", "golotest.execution.FunctionsWithReturns");

    Method emptyFunction = moduleClass.getMethod("empty");
    assertThat(isPublic(emptyFunction.getModifiers()), is(true));
    assertThat(isStatic(emptyFunction.getModifiers()), is(true));
    assertThat(emptyFunction.getParameterTypes().length, is(0));
    assertThat(emptyFunction.invoke(null), nullValue());

    Method directReturn = moduleClass.getMethod("direct_return");
    assertThat(isPublic(directReturn.getModifiers()), is(true));
    assertThat(isStatic(directReturn.getModifiers()), is(true));
    assertThat(directReturn.getParameterTypes().length, is(0));
    assertThat(directReturn.invoke(null), nullValue());

    Method ignoreMe = moduleClass.getDeclaredMethod("ignore_me");
    assertThat(isPrivate(ignoreMe.getModifiers()), is(true));
    assertThat(isStatic(ignoreMe.getModifiers()), is(true));

    Method fortyTwo = moduleClass.getMethod("return_42");
    assertThat((Integer) fortyTwo.invoke(null), is(42));

    Method helloWorld = moduleClass.getMethod("return_hello_world");
    assertThat((String) helloWorld.invoke(null), is("Hello, world!"));
  }

  @Test
  public void test_parameterless_function_calls() throws ClassNotFoundException, IOException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Class<?> moduleClass = load("parameterless-function-calls.golo", "golotest.execution.ParameterLessFunctionCalls");

    Method call_hello = moduleClass.getMethod("call_hello");
    assertThat((String) call_hello.invoke(null), is("hello()"));

    Method call_now = moduleClass.getMethod("call_now");
    assertThat(((Long) call_now.invoke(null)) > 0, is(true));
  }
}