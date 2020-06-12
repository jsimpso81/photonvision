package com.chameleonvision.common.vision.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.chameleonvision.common.calibration.CameraCalibrationCoefficients;
import com.chameleonvision.common.util.TestUtils;
import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.provider.FileFrameProvider;
import com.chameleonvision.common.vision.opencv.CVMat;
import com.chameleonvision.common.vision.opencv.ContourGroupingMode;
import com.chameleonvision.common.vision.opencv.ContourIntersectionDirection;
import com.chameleonvision.common.vision.target.TargetModel;
import com.chameleonvision.common.vision.target.TrackedTarget;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SolvePNPTest {

    private static final String LIFECAM_240P_CAL_FILE = "lifecam240p.json";
    private static final String LIFECAM_480P_CAL_FILE = "lifecam480p.json";

    @BeforeEach
    public void Init() {
        TestUtils.loadLibraries();
    }

    @Test
    public void loadCameraIntrinsics() {
        var lifecam240pCal = getCoeffs(LIFECAM_240P_CAL_FILE);
        var lifecam480pCal = getCoeffs(LIFECAM_480P_CAL_FILE);

        assertNotNull(lifecam240pCal);
        checkCameraCoefficients(lifecam240pCal);
        assertNotNull(lifecam480pCal);
        checkCameraCoefficients(lifecam480pCal);
    }

    private CameraCalibrationCoefficients getCoeffs(String filename) {
        try {
            var cameraCalibration =
                    new ObjectMapper()
                            .readValue(
                                    (Path.of(TestUtils.getCalibrationPath().toString(), filename).toFile()),
                                    CameraCalibrationCoefficients.class);

            checkCameraCoefficients(cameraCalibration);

            return cameraCalibration;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void checkCameraCoefficients(CameraCalibrationCoefficients cameraCalibration) {
        assertEquals(3, cameraCalibration.cameraIntrinsics.rows);
        assertEquals(3, cameraCalibration.cameraIntrinsics.cols);
        assertEquals(3, cameraCalibration.cameraIntrinsics.getAsMat().rows());
        assertEquals(3, cameraCalibration.cameraIntrinsics.getAsMat().cols());
        assertEquals(3, cameraCalibration.cameraIntrinsics.getAsMatOfDouble().rows());
        assertEquals(3, cameraCalibration.cameraIntrinsics.getAsMatOfDouble().cols());
        assertEquals(3, cameraCalibration.getCameraIntrinsicsMat().rows());
        assertEquals(3, cameraCalibration.getCameraIntrinsicsMat().cols());
        assertEquals(1, cameraCalibration.cameraExtrinsics.rows);
        assertEquals(5, cameraCalibration.cameraExtrinsics.cols);
        assertEquals(1, cameraCalibration.cameraExtrinsics.getAsMat().rows());
        assertEquals(5, cameraCalibration.cameraExtrinsics.getAsMat().cols());
        assertEquals(1, cameraCalibration.cameraExtrinsics.getAsMatOfDouble().rows());
        assertEquals(5, cameraCalibration.cameraExtrinsics.getAsMatOfDouble().cols());
        assertEquals(1, cameraCalibration.getCameraExtrinsicsMat().rows());
        assertEquals(5, cameraCalibration.getCameraExtrinsicsMat().cols());
    }

    @Test
    public void test2019() {
        var pipeline = new ReflectivePipeline();

        pipeline.getSettings().hsvHue.set(60, 100);
        pipeline.getSettings().hsvSaturation.set(100, 255);
        pipeline.getSettings().hsvValue.set(190, 255);
        pipeline.getSettings().outputShowThresholded = true;
        pipeline.getSettings().outputShowMultipleTargets = true;
        pipeline.getSettings().solvePNPEnabled = true;
        pipeline.getSettings().contourGroupingMode = ContourGroupingMode.Dual;
        pipeline.getSettings().contourIntersection = ContourIntersectionDirection.Up;
        pipeline.getSettings().cornerDetectionUseConvexHulls = true;
        pipeline.getSettings().targetModel = TargetModel.get2019Target();
        pipeline.getSettings().cameraCalibration = getCoeffs(LIFECAM_240P_CAL_FILE);

        var frameProvider =
                new FileFrameProvider(
                        TestUtils.getWPIImagePath(TestUtils.WPI2019Image.kCargoStraightDark48in),
                        TestUtils.WPI2019Image.FOV);

        CVPipelineResult pipelineResult;

        pipelineResult = pipeline.run(frameProvider.get());
        printTestResults(pipelineResult);

        // these numbers are not *accurate*, but they are known and expected
        var pose = pipelineResult.targets.get(0).getRobotRelativePose();
        assertEquals(41.96, pose.getTranslation().getX(), 0.05);
        assertEquals(-1.03, pose.getTranslation().getY(), 0.05);
        assertEquals(1.46, pose.getRotation().getDegrees(), 0.05);

        TestUtils.showImage(pipelineResult.outputFrame.image.getMat(), "Pipeline output", 1000 * 90);
    }

    @Test
    public void test2020() {
        var pipeline = new ReflectivePipeline();

        pipeline.getSettings().hsvHue.set(60, 100);
        pipeline.getSettings().hsvSaturation.set(100, 255);
        pipeline.getSettings().hsvValue.set(60, 255);
        pipeline.getSettings().outputShowThresholded = true;
        pipeline.getSettings().solvePNPEnabled = true;
        pipeline.getSettings().cornerDetectionAccuracyPercentage = 4;
        pipeline.getSettings().cornerDetectionUseConvexHulls = true;
        pipeline.getSettings().cameraCalibration = getCoeffs(LIFECAM_480P_CAL_FILE);
        pipeline.getSettings().targetModel = TargetModel.get2020Target(36);
        pipeline.getSettings().cameraPitch = Rotation2d.fromDegrees(0.0);

        var frameProvider =
                new FileFrameProvider(
                        TestUtils.getWPIImagePath(TestUtils.WPI2020Image.kBlueGoal_224in_Left),
                        TestUtils.WPI2020Image.FOV);

        CVPipelineResult pipelineResult = pipeline.run(frameProvider.get());
        printTestResults(pipelineResult);

        // these numbers are not *accurate*, but they are known and expected
        var pose = pipelineResult.targets.get(0).getRobotRelativePose();
        assertEquals(260.26, pose.getTranslation().getX(), 0.05);
        assertEquals(64.26, pose.getTranslation().getY(), 0.05);
        assertEquals(36.88, pose.getRotation().getDegrees(), 0.05);

        TestUtils.showImage(pipelineResult.outputFrame.image.getMat(), "Pipeline output", 999999);
    }

    //    @Test
    //    public void junk() {
    //        var frameProvider =
    //                new FileFrameProvider(
    //
    // TestUtils.getWPIImagePath(TestUtils.WPI2019Image.kCargoStraightDark72in_HighRes),
    //                        TestUtils.WPI2019Image.FOV);
    //
    //        var settings = new ReflectivePipelineSettings();
    //        settings.hsvHue.set(60, 100);
    //        settings.hsvSaturation.set(100, 255);
    //        settings.hsvValue.set(190, 255);
    //        settings.outputShowThresholded = true;
    //        settings.outputShowMultipleTargets = true;
    //        settings.contourGroupingMode = ContourGroupingMode.Dual;
    //        settings.contourIntersection = ContourIntersectionDirection.Up;
    //
    //        continuouslyRunPipeline(frameProvider.getFrame(), settings);
    //    }

    private static void continuouslyRunPipeline(Frame frame, ReflectivePipelineSettings settings) {
        var pipeline = new ReflectivePipeline();
        pipeline.settings = settings;

        while (true) {
            CVPipelineResult pipelineResult = pipeline.run(frame);
            printTestResults(pipelineResult);
            int preRelease = CVMat.getMatCount();
            pipelineResult.release();
            int postRelease = CVMat.getMatCount();

            System.out.printf("Pre: %d, Post: %d\n", preRelease, postRelease);
        }
    }

    // used to run VisualVM for profiling, which won't run on unit tests.
    public static void main(String[] args) {
        TestUtils.loadLibraries();
        var frameProvider =
                new FileFrameProvider(
                        TestUtils.getWPIImagePath(TestUtils.WPI2019Image.kCargoStraightDark72in_HighRes),
                        TestUtils.WPI2019Image.FOV);

        var settings = new ReflectivePipelineSettings();
        settings.hsvHue.set(60, 100);
        settings.hsvSaturation.set(100, 255);
        settings.hsvValue.set(190, 255);
        settings.outputShowThresholded = true;
        settings.outputShowMultipleTargets = true;
        settings.contourGroupingMode = ContourGroupingMode.Dual;
        settings.contourIntersection = ContourIntersectionDirection.Up;

        continuouslyRunPipeline(frameProvider.get(), settings);
    }

    private static void printTestResults(CVPipelineResult pipelineResult) {
        double fps = 1000 / pipelineResult.getLatencyMillis();
        System.out.println(
                "Pipeline ran in " + pipelineResult.getLatencyMillis() + "ms (" + fps + " " + "fps)");
        System.out.println("Found " + pipelineResult.targets.size() + " valid targets");
        System.out.println(
                "Found targets at "
                        + pipelineResult.targets.stream()
                                .map(TrackedTarget::getRobotRelativePose)
                                .collect(Collectors.toList()));
    }
}