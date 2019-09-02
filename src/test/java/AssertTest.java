

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.com.juli0mendes.domains.Usuario;

public class AssertTest {

	@Test
	public void test() {
		
		assertTrue(true);
		assertFalse(false);
		
		assertEquals(1, 1);
		assertNotEquals(1, 2);
		assertEquals(0.5122, 0.51, 0.01);
		assertEquals(Math.PI, 3.14, 0.01);
		
		int i = 5;
		Integer integer = 5;
		assertEquals(Integer.valueOf(i), integer);
		assertEquals(i, integer.intValue());
		
		assertEquals("bola", "bola");
		assertTrue("bola".equalsIgnoreCase("Bola"));
		assertTrue("bola".startsWith("bo"));
		
		Usuario usuario1 = new Usuario("U1");
		Usuario usuario2 = new Usuario("U1");
		Usuario usuario3 = usuario2;
		Usuario usuario4 = null;
		
		assertEquals(usuario1, usuario2);
		
		assertSame(usuario2, usuario2);
		assertSame(usuario2, usuario3);
		assertNull(usuario4);
		assertNotNull(usuario3);
		
	}
}
