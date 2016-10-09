package com.rest.api.service;

import com.rest.api.util.ClassNameUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

//@Service
@Component
final public class ScriptService {

    final private ScriptEngine engine;
    private Thread currThread;
    final private ReentrantLock lock;
    private StringWriter stringWriter = null;
    private static final Logger logger = Logger.getLogger(ClassNameUtil.getCurrentClassName());

    public ScriptService() {
        lock = new ReentrantLock();
        engine = new ScriptEngineManager().getEngineByName("nashorn"); //JavaScript or js
    }

    public String addEvalScript(String bodyScript) {
        logger.info("Enter to eval method - " + Thread.currentThread().getName());
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                logger.info("Enter to lock - " + Thread.currentThread().getName());
                currThread = Thread.currentThread();
                stringWriter = new StringWriter();
                engine.getContext().setWriter(stringWriter);
                try {
                    engine.eval(bodyScript);
                } catch (ScriptException e) {
                    logger.info(e.getMessage());
                    return "ScriptException " + e.getMessage();
                }
            } else {
                return "503";
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            return "Waiting thread was interrupted";
        } finally {
            if (currThread.isAlive() && lock.isHeldByCurrentThread()) {
                logger.info("Exit from lock -  " + Thread.currentThread().getName());
                lock.unlock();
            }
        }
        logger.info("Exit from eval method - " + Thread.currentThread().getName());
        return "Script  evaluated\nOutput:\n" + stringWriter.toString();
    }

    public String invokeFunction(String funcName) {
        logger.info("Enter to invoke method - " + funcName + "     " + Thread.currentThread().getName());
        Object result;
        Invocable invocable;
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                logger.info("Enter to lock - " + funcName + "     " + Thread.currentThread().getName());
                currThread = Thread.currentThread();
                stringWriter = new StringWriter();
                engine.getContext().setWriter(stringWriter);
                if (engine instanceof Invocable) {
                    invocable = (Invocable) engine;
                    try {
                        if (engine.getBindings(100).get(funcName) != null) {
                            result = invocable.invokeFunction(funcName);
                            logger.info("Exit from invoke method - " + funcName + "     " + Thread.currentThread().getName());
                            return "Output:\n" + stringWriter.toString() + "\nResult is:\n" + result.toString();
                        }
                    } catch (ScriptException | NoSuchMethodException e) {
                        logger.error(e.getMessage());
                        lock.unlock();
                        return e.getMessage();
                    }
                }
            } else {
                return "503";
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            return "Waiting thread was interrupted";
        } finally {
            if (currThread.isAlive() && lock.isHeldByCurrentThread()) {
                logger.info("Exit from lock  -  " + funcName + "     " + Thread.currentThread().getName());
                lock.unlock();
            }
        }
        return "Function " + funcName + " is absent";
    }

    @SuppressWarnings("deprecation")
    public String interruptEngine() {

        if (currThread.isAlive()) {
            currThread.stop();
            return "Thread was stoped. \nOutput: \n" + stringWriter.toString();
        }
        return "Nothing to interrupt. Engine ready to use";

    }

    public String getStatus() {
        if (currThread.isAlive()) {
            return "Nashorn is working.";
        } else {
            return "Nashorn is ready to use.";
        }
    }

    public String getBindings() {
        return engine.getBindings(100).entrySet().toString();
    }

}
