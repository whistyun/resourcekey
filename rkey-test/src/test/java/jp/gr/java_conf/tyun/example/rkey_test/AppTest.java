package jp.gr.java_conf.tyun.example.rkey_test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jp.co.java_conf.tyun.example.rkey_test.gen.AKey;
import jp.co.java_conf.tyun.example.rkey_test.gen.BKey;

/**
 * Unit test for simple App.
 */
public class AppTest {

	@Test
	public void test_AKey() {
		assertEquals("foo", AKey.elem1.getString());
		assertEquals("bar", AKey.elem2.getString());
		assertEquals("hoge", AKey.test2.elem1.getString());
		assertEquals("fuga", AKey.test2.elem2.getString());
		assertEquals("abcd", AKey.test3.elem1.getString());
		assertEquals("abcd", AKey.test3.elem1.getString());
		assertEquals("efgh", AKey.test3.elem2.getString());
		assertEquals("ijkl", AKey.test3.elem3.getString());
		assertEquals("x", AKey.test3.telem3.p1.getString());
		assertEquals("y", AKey.test3.telem3.p2.getString());
		assertEquals("z", AKey.test3.telem3.p3.getString());
	}

	@Test
	public void test_BKey() {
		assertEquals("foo", BKey.belem1.getString());
		assertEquals("bar", BKey.belem2.getString());
		assertEquals("hoge", BKey.btest2.elem1.getString());
		assertEquals("fuga", BKey.btest2.elem2.getString());
		assertEquals("abcd", BKey.btest3.elem1.getString());
		assertEquals("abcd", BKey.btest3.elem1.getString());
		assertEquals("efgh", BKey.btest3.elem2.getString());
		assertEquals("ijkl", BKey.btest3.elem3.getString());
		assertEquals("x", BKey.btest3.telem3.p1.getString());
		assertEquals("y", BKey.btest3.telem3.p2.getString());
		assertEquals("z", BKey.btest3.telem3.p3.getString());
	}
}
