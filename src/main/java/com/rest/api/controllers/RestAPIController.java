package com.rest.api.controllers;

import com.rest.api.service.ScriptService;
import com.rest.api.util.ClassNameUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.*;

@RestController
@RequestMapping(value = "/api")
public class RestAPIController {

    private static final Logger logger = Logger.getLogger(ClassNameUtil.getCurrentClassName());

    @Autowired
    ScriptService scriptService;

    @RequestMapping(value = "/procScript", method = RequestMethod.POST, produces = "text/plain")
    public DeferredResult<String> processingInputScript(@RequestBody String inputScript, HttpServletResponse response) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        DeferredResult<String> deferredResult = new DeferredResult<>();
        FutureTask<String> futureTask = new FutureTask<>(() -> scriptService.addEvalScript(inputScript));
        executorService.execute(futureTask);

        try {
            deferredResult.setResult(futureTask.get(15L, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            logger.warn("InterruptedException:   " + e.getMessage());
            response.setStatus(500);
            deferredResult.setResult("Stoped: InterruptedException");
            return deferredResult;
        } catch (TimeoutException e) {
            logger.warn("TimeoutException:   " + e.getMessage());
            scriptService.interruptEngine();
            response.setStatus(500);
            deferredResult.setResult("Stoped: TimeoutException");
            return deferredResult;
        } catch (ExecutionException e) {
            logger.warn("ExecutionException:   " + e.getMessage());
            response.setStatus(500);
            deferredResult.setResult("Stoped: ExecutionException");
            return deferredResult;
        } finally {
            executorService.shutdown();
        }
        if (deferredResult.getResult().toString().equals("503")) {
            deferredResult.setResult("Engine is busy");
            response.setStatus(503);
        }
        return deferredResult;
    }


    @RequestMapping(value = "/function/{funcName}", method = RequestMethod.POST, produces = "text/plain")
    public DeferredResult<String> callFunctionJS(@PathVariable String funcName, HttpServletResponse response) {
        String result = null;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        DeferredResult<String> deferredResult = new DeferredResult<>();
        FutureTask<String> futureTask = new FutureTask<>(() -> scriptService.invokeFunction(funcName));
        executor.execute(futureTask);

        try {
            result = futureTask.get(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("InterruptedException:   " + e.getMessage());
            response.setStatus(500);
            deferredResult.setResult("Stoped: InterruptedException");
            return deferredResult;
        } catch (TimeoutException e) {
            logger.warn("TimeoutException:   " + e.getMessage());
            scriptService.interruptEngine();
            response.setStatus(500);
            deferredResult.setResult("Stoped: TimeoutException");
            return deferredResult;
        } catch (ExecutionException e) {
            logger.warn("ExecutionException:   " + e.getMessage());
            response.setStatus(500);
            deferredResult.setResult("Stoped: ExecutionException");
            return deferredResult;
        } finally {
            executor.shutdown();
        }

        if (result.equals("503")) {
            result = "Engine is busy";
            response.setStatus(503);
        }
        deferredResult.setResult(result);
        return deferredResult;
    }

    @RequestMapping(value = "/getBindings", method = RequestMethod.POST, produces = "text/plain")
    public String getBindings() {
        return scriptService.getBindings();
    }

    @RequestMapping(value = "/stopTask", method = RequestMethod.POST, produces = "text/plain")
    public String stopTask(HttpServletResponse response) {
        response.setStatus(500);
        return scriptService.interruptEngine();
    }

    @RequestMapping(value = "/getStatus", method = RequestMethod.GET, produces = "text/plain")
    public String getStatus() {
        return scriptService.getStatus();
    }

}
