/*
 * Copyright (C) 2020 Photon Vision.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.photonvision.common.hardware.metrics;

import java.util.HashMap;
import org.photonvision.common.dataflow.DataChangeService;
import org.photonvision.common.dataflow.events.OutgoingUIEvent;
import org.photonvision.common.logging.LogGroup;
import org.photonvision.common.logging.Logger;
import org.photonvision.common.util.TimedTaskManager;
import org.photonvision.server.UIUpdateType;

public class MetricsPublisher {
    private final HashMap<String, Double> metrics;
    private static final Logger logger = new Logger(MetricsPublisher.class, LogGroup.General);
    private static CPU cpu;
    private static GPU gpu;
    private static RAM ram;

    public static MetricsPublisher getInstance() {
        return Singleton.INSTANCE;
    }

    private MetricsPublisher() {
        cpu = CPU.getInstance();
        gpu = GPU.getInstance();
        ram = RAM.getInstance();

        metrics = new HashMap<>();
    }

    public void startTask() {
        TimedTaskManager.getInstance()
                .addTask(
                        "Metrics",
                        () -> {
                            metrics.put("cpuTemp", cpu.getTemp());
                            metrics.put("cpuUtil", cpu.getUtilization());
                            metrics.put("cpuMem", cpu.getMemory());
                            metrics.put("gpuTemp", gpu.getTemp());
                            metrics.put("gpuMem", gpu.getMemory());
                            metrics.put("ramUtil", ram.getUsedRam());

                            DataChangeService.getInstance()
                                    .publishEvent(
                                            new OutgoingUIEvent<>(UIUpdateType.BROADCAST, "metrics", metrics, null));
                        },
                        1000);
    }

    public void stopTask() {
        TimedTaskManager.getInstance().cancelTask("Metrics");
        logger.info("This device does not support running bash commands. Stopped metrics thread.");
    }

    private static class Singleton {
        public static final MetricsPublisher INSTANCE = new MetricsPublisher();
    }
}