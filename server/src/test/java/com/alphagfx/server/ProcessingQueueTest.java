package com.alphagfx.server;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ProcessingQueueTest {

    private ProcessingQueue<Object> queue;

    public ProcessingQueueTest(ProcessingQueue queue) {
        this.queue = queue;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> instancesToTest() {
        return Arrays.asList(new Object[][]{new Object[]{new ProcessingQueueImpl()}});
    }

    @Test
    public void testAddAndPoll() {
        User obj1 = new User(null);
        User obj2 = new User(null);

        assertTrue(queue.add(obj1));
        assertEquals(obj1, queue.poll());

        assertNull(queue.poll());

        assertTrue(queue.add(obj2));
        assertTrue(queue.add(obj1));

        assertEquals(obj2, queue.poll());
        assertEquals(obj1, queue.poll());
    }

}