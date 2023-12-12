package cordori.dpstamina.task;

import cordori.dpstamina.manager.SQLManager;

public class SQLScheduler implements Runnable {
    @Override
    public void run() {
        SQLManager.sql.saveAllData();
    }
}
