import com.rest.api.service.FutureTaskService;
import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ScriptServiceTest {

    public static final int EXECUTION_TIMEOUT_SECONDS = 2;

    @Test
    public void testStoppableFuture() throws ExecutionException, InterruptedException {
        FutureTaskService<Integer> task = new FutureTaskService<>( () -> {
            int i = 0;
            while(i < 250_000) {
                try {
                    i++;
                    // comment out next line to see Thread.stop() in action. Thread.sleep() is interruptible, whereas tight calculation cycle isn't
                    // Thread.sleep(100);
                    System.out.println(i);
//                } catch (InterruptedException e) {
//                    System.err.println("Interrupted " + Thread.currentThread().getName());
//                    e.printStackTrace(System.err);
//                    throw e;
                } catch (ThreadDeath d) {
                    System.err.println("Stopped " + Thread.currentThread().getName());
                    d.printStackTrace(System.err);
                    throw d;
                }
            }
            return i;
        });
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(task);
        try {
            task.get(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertFalse("We expect exception to be thrown", true);
        } catch (TimeoutException e) {
            task.stop();
        }
        assertTrue(task.isCancelled());
    }
}
