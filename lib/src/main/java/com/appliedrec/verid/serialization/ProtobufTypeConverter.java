package com.appliedrec.verid.serialization;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;

import com.appliedrec.verid.core2.EulerAngle;
import com.appliedrec.verid.core2.FaceDetection;
import com.appliedrec.verid.core2.FaceDetectionRecognitionSettings;
import com.appliedrec.verid.core2.FaceRecognition;
import com.appliedrec.verid.core2.VerID;
import com.appliedrec.verid.core2.VerIDCoreException;
import com.appliedrec.verid.core2.VerIDFaceTemplateVersion;
import com.appliedrec.verid.proto.Capture;
import com.appliedrec.verid.proto.DeviceInfo;
import com.appliedrec.verid.proto.Face;
import com.appliedrec.verid.proto.Image;
import com.appliedrec.verid.proto.Point;
import com.appliedrec.verid.proto.RecognizableFace;
import com.appliedrec.verid.proto.Rect;
import com.appliedrec.verid.proto.SystemInfo;
import com.appliedrec.verid.proto.VeridSettings;
import com.google.common.collect.FluentIterable;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

public class ProtobufTypeConverter {

    public Face convertFace(com.appliedrec.verid.core2.Face face) {
        com.appliedrec.verid.proto.Face.Builder builder = com.appliedrec.verid.proto.Face.newBuilder()
                .setBounds(Rect.newBuilder().setOrigin(Point.newBuilder().setX(face.getBounds().left).setY(face.getBounds().top)).setSize(com.appliedrec.verid.proto.Size.newBuilder().setWidth(face.getBounds().width()).setHeight(face.getBounds().height())))
                .setAngle(com.appliedrec.verid.proto.EulerAngle.newBuilder()
                .setYaw(face.getAngle().getYaw())
                .setPitch(face.getAngle().getPitch())
                .setRoll(face.getAngle().getRoll()))
                .setQuality(face.getQuality())
                .setSerialized(ByteString.copyFrom(face.getData()));
        if (face.getLeftEye() != null) {
            builder.setLeftEye(Point.newBuilder().setX(face.getLeftEye().x).setY(face.getLeftEye().y));
        }
        if (face.getRightEye() != null) {
            builder.setRightEye(Point.newBuilder().setX(face.getRightEye().x).setY(face.getRightEye().y));
        }
        if (face.getLandmarks() != null) {
            for (PointF landmark : face.getLandmarks()) {
                builder.addLandmarks(Point.newBuilder().setX(landmark.x).setY(landmark.y));
            }
        }
        if (face.getDlibLandmarks() != null) {
            for (float landmark : face.getDlibLandmarks()) {
                builder.addDlibLandmarks(landmark);
            }
        }
        return builder.build();
    }

    public com.appliedrec.verid.core2.Face convertFace(Face protoFace) {
        byte[] data = protoFace.getSerialized().toByteArray();
        RectF bounds = new RectF(protoFace.getBounds().getOrigin().getX(), protoFace.getBounds().getOrigin().getY(), protoFace.getBounds().getOrigin().getX()+protoFace.getBounds().getSize().getWidth(), protoFace.getBounds().getOrigin().getY()+protoFace.getBounds().getSize().getHeight());
        PointF leftEye = new PointF(protoFace.getLeftEye().getX(), protoFace.getLeftEye().getY());
        PointF rightEye = new PointF(protoFace.getRightEye().getX(), protoFace.getRightEye().getY());
        EulerAngle angle = new EulerAngle(protoFace.getAngle().getYaw(), protoFace.getAngle().getPitch(), protoFace.getAngle().getRoll());
        float quality = protoFace.getQuality();
        PointF[] landmarks = FluentIterable.from(protoFace.getLandmarksList()).transform(pt -> new PointF(pt.getX(), pt.getY())).toArray(PointF.class);
        float[] dlibLandmarks = new float[protoFace.getDlibLandmarksCount()];
        for (int i=0; i<dlibLandmarks.length; i++) {
            dlibLandmarks[i] = protoFace.getDlibLandmarks(i);
        }
        return new com.appliedrec.verid.core2.Face(bounds, angle, leftEye, rightEye, data, quality, landmarks, dlibLandmarks);
    }

    public RecognizableFace convertRecognizableFace(com.appliedrec.verid.core2.RecognizableFace face) {
        Face protoFace = convertFace(face);
        return RecognizableFace.newBuilder()
                .setFace(protoFace)
                .setTemplate(com.appliedrec.verid.proto.FaceTemplate.newBuilder()
                        .setData(ByteString.copyFrom(face.getRecognitionData()))
                        .setVersion(face.getVersion()))
                .build();
    }

    public com.appliedrec.verid.core2.RecognizableFace convertRecognizableFace(RecognizableFace face) throws VerIDCoreException {
        com.appliedrec.verid.core2.Face veridFace = convertFace(face.getFace());
        return new com.appliedrec.verid.core2.RecognizableFace(veridFace, face.getTemplate().getData().toByteArray(), VerIDFaceTemplateVersion.fromSerialNumber(face.getTemplate().getVersion()));
    }

    public Image convertImage(com.appliedrec.verid.core2.Image image) {
        return Image.newBuilder()
                .setData(ByteString.copyFrom(image.getData()))
                .setWidth(image.getWidth())
                .setHeight(image.getHeight())
                .setOrientation(image.getOrientation())
                .setBytesPerRow(image.getBytesPerRow())
                .setFormat(com.appliedrec.verid.proto.Image.ImageFormat.valueOf(image.getFormat().getName()))
                .build();
    }

    public com.appliedrec.verid.core2.Image convertImage(Image image) {
        byte[] data = image.getData().toByteArray();
        int width = image.getWidth();
        int height = image.getHeight();
        int orientation = image.getOrientation();
        int bytesPerRow = image.getBytesPerRow();
        com.appliedrec.verid.core2.ImageFormat format = com.appliedrec.verid.core2.ImageFormat.valueOf(image.getFormat().name());
        return new com.appliedrec.verid.core2.Image(data, width, height, orientation, bytesPerRow, format);
    }

    public DeviceInfo getCurrentDeviceInfo() {
        return DeviceInfo.newBuilder()
                .setMake(Build.MANUFACTURER)
                .setModel(Build.MODEL)
                .setOs("Android")
                .setOsVersion(Build.VERSION.RELEASE)
                .build();
    }

    public VeridSettings getVeridSettings(VerID verid) {
        if (!(verid.getFaceRecognition() instanceof FaceRecognition)) {
            return VeridSettings.getDefaultInstance();
        }
        FaceDetectionRecognitionSettings settings = ((FaceDetection)verid.getFaceDetection()).detRecLib.getSettings();
        int faceTemplateVersion = ((FaceRecognition)verid.getFaceRecognition()).defaultFaceTemplateVersion.getValue();
        return VeridSettings.newBuilder()
                .setAttemptMultiThreading(settings.getAttemptMultiThreading())
                .setConfidenceThreshold(settings.getConfidenceThreshold())
                .setDefaultTemplateVersion(faceTemplateVersion)
                .setDetectSmile(settings.getDetectSmile())
                .setEyeDetectionVariant(settings.getEyeDetectionVariant())
                .setFaceExtractQualityThreshold(settings.getFaceExtractQualityThreshold())
                .setLandmarkOptions(settings.getLandmarkOptions())
                .setLandmarkTrackingQualityThreshold(settings.getLandmarkTrackingQualityThreshold())
                .setLightingCompensation(settings.getLightingCompensation())
                .setLightingMatrix(settings.getLightingMatrix())
                .setMatrixTemplateVersion(settings.getMatrixTemplateVersion())
                .setPoseCompensation(settings.getPoseCompensation())
                .setPoseVariant(settings.getPoseVariant())
                .setQualityThreshold(settings.getFaceExtractQualityThreshold())
                .setReduceConfidenceCalculation(settings.getReduceConfidenceCalculation())
                .setRollRangeLarge(settings.getRollRangeLarge())
                .setRollRangeSmall(settings.getRollRangeSmall())
                .setSizeRange(settings.getSizeRange())
                .setYawRangeLarge(settings.getYawRangeLarge())
                .setYawRangeSmall(settings.getYawRangeSmall())
                .setYawPitchVariant(settings.getYawPitchVariant())
                .build();
    }

    public SystemInfo getCurrentSystemInfo(VerID verid) {
        return SystemInfo.newBuilder()
                .setDeviceInfo(getCurrentDeviceInfo())
                .setVeridVersion(VerID.getVersion())
                .setVeridSettings(getVeridSettings(verid))
                .setFaceDetectionClassName(verid.getFaceDetection().getClass().getName())
                .setFaceRecognitionClassName(verid.getFaceRecognition().getClass().getName())
                .setUserManagementClassName(verid.getUserManagement().getClass().getName())
                .build();
    }

    public Capture createCapture(Date date, com.appliedrec.verid.core2.Image image, com.appliedrec.verid.core2.RecognizableFace[] faces, Bitmap bitmap, SystemInfo systemInfo) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            byte[] imageBytes = outputStream.toByteArray();
            Capture.Builder builder = Capture.newBuilder()
                    .setVeridImage(convertImage(image))
                    .setDate(Timestamp.newBuilder().setSeconds(date.getTime() / 1000).build())
                    .setImage(ByteString.copyFrom(imageBytes))
                    .setSystemInfo(systemInfo);
            if (faces.length > 0) {
                outputStream.reset();
                android.graphics.Rect faceRect = new android.graphics.Rect();
                faces[0].getBounds().round(faceRect);
                faceRect.intersect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                Bitmap faceImage = Bitmap.createBitmap(bitmap, faceRect.left, faceRect.top, faceRect.width(), faceRect.height());
                faceImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                builder.setFaceImage(ByteString.copyFrom(outputStream.toByteArray()));
            }
            for (com.appliedrec.verid.core2.RecognizableFace face : faces) {
                builder.addFaces(convertRecognizableFace(face));
            }
            return builder.build();
        }
    }
}
