package com.appliedrec.verid.serialization;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import androidx.exifinterface.media.ExifInterface;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.appliedrec.verid.core2.EulerAngle;
import com.appliedrec.verid.core2.Face;
import com.appliedrec.verid.core2.Image;
import com.appliedrec.verid.core2.RecognizableFace;
import com.appliedrec.verid.core2.VerIDCoreException;
import com.appliedrec.verid.core2.VerIDFaceTemplateVersion;
import com.appliedrec.verid.proto.FaceTemplate;
import com.appliedrec.verid.proto.Point;
import com.appliedrec.verid.proto.Rect;
import com.appliedrec.verid.proto.Size;
import com.google.protobuf.ByteString;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SerializationTest {

    private ProtobufTypeConverter converter;
    private RectF bounds;
    private EulerAngle angle;
    private PointF leftEye;
    private PointF rightEye;
    private byte[] faceData;
    private float faceQuality;
    private PointF[] landmarks;
    private float[] dlibLandmarks;

    @Before
    public void createConverter() {
        converter = new ProtobufTypeConverter();
        bounds = new RectF(20, 30, 40, 50);
        angle = new EulerAngle(1,2,3);
        leftEye = new PointF(25, 35);
        rightEye = new PointF(35, 35);
        faceData = new byte[128];
        for (int i=0; i<faceData.length; i++) {
            faceData[i] = (byte)i;
        }
        faceQuality = 9.9f;
        landmarks = new PointF[68];
        for (int i=0; i<landmarks.length; i++) {
            landmarks[i] = new PointF(i, i);
        }
        dlibLandmarks = new float[10];
        for (int i=0; i<dlibLandmarks.length; i++) {
            dlibLandmarks[i] = (float)i;
        }
    }

    @Test
    public void testVerIDFaceToProtobufFaceConversion() {
        Face veridFace = createVerIDFace();
        com.appliedrec.verid.proto.Face protoFace = converter.convertFace(veridFace);
        compareFaces(veridFace, protoFace);
    }

    @Test
    public void testProtobufFaceToVerIDFaceConversion() throws Exception {
        com.appliedrec.verid.proto.Face protoFace = createProtoFace();
        Face veridFace = converter.convertFace(protoFace);
        compareFaces(veridFace, protoFace);
    }

    @Test
    public void testVerIDRecognizableFaceToProtobufRecognizableFaceConversion() {
        RecognizableFace veridFace = createVerIDRecognizableFace();
        com.appliedrec.verid.proto.RecognizableFace protoFace = converter.convertRecognizableFace(veridFace);
        compareRecognizableFaces(veridFace, protoFace);
    }

    @Test
    public void testProtobufRecognizableFaceToVerIDRecognizableFaceConversion() throws VerIDCoreException {
        com.appliedrec.verid.proto.RecognizableFace protoFace = createProtoRecognizableFace();
        RecognizableFace veridFace = converter.convertRecognizableFace(protoFace);
        compareRecognizableFaces(veridFace, protoFace);
    }

    @Test
    public void testVerIDImageToProtobufImageConversion() {
        Image veridImage = createVerIDImage();
        com.appliedrec.verid.proto.Image protoImage = converter.convertImage(veridImage);
        compareImages(veridImage, protoImage);
    }

    @Test
    public void testProtobufImageToVerIDImageConversion() {
        com.appliedrec.verid.proto.Image protoImage = createProtoImage();
        Image veridImage = converter.convertImage(protoImage);
        compareImages(veridImage, protoImage);
    }

    private Image createVerIDImage() {
        return new Image(createTestBitmap(), ExifInterface.ORIENTATION_NORMAL);
    }

    private com.appliedrec.verid.proto.Image createProtoImage() {
        Bitmap bitmap = createTestBitmap();
        byte[] imageBytes = new byte[bitmap.getWidth()*4*bitmap.getHeight()];
        ByteBuffer imageBuffer = ByteBuffer.wrap(imageBytes);
        bitmap.copyPixelsToBuffer(imageBuffer);
        return com.appliedrec.verid.proto.Image.newBuilder()
                .setFormat(com.appliedrec.verid.proto.Image.ImageFormat.RGBA)
                .setOrientation(ExifInterface.ORIENTATION_NORMAL)
                .setWidth(bitmap.getWidth())
                .setHeight(bitmap.getHeight())
                .setBytesPerRow(bitmap.getWidth()*4)
                .setData(ByteString.copyFrom(imageBytes))
                .build();
    }

    private Bitmap createTestBitmap() {
        int width = 100;
        int height = 200;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint red = new Paint();
        red.setStyle(Paint.Style.FILL);
        red.setColor(Color.RED);
        Paint green = new Paint();
        green.setStyle(Paint.Style.FILL);
        green.setColor(Color.GREEN);
        Paint blue = new Paint();
        blue.setStyle(Paint.Style.FILL);
        blue.setColor(Color.BLUE);
        Paint yellow = new Paint();
        yellow.setStyle(Paint.Style.FILL);
        yellow.setColor(Color.YELLOW);
        canvas.drawRect(0, 0, width/2f, height/2f, red);
        canvas.drawRect(width/2f, 0, width, height/2f, green);
        canvas.drawRect(width/2f, height/2f, width, height, blue);
        canvas.drawRect(0, height/2f, width/2f, height, yellow);
        return bitmap;
    }

    private com.appliedrec.verid.proto.Face createProtoFace() {
        com.appliedrec.verid.proto.Face.Builder faceBuilder = com.appliedrec.verid.proto.Face.newBuilder()
                .setBounds(Rect.newBuilder().setOrigin(Point.newBuilder().setX(bounds.left).setY(bounds.top)).setSize(Size.newBuilder().setWidth(bounds.width()).setHeight(bounds.height())))
                .setAngle(com.appliedrec.verid.proto.EulerAngle.newBuilder().setYaw(angle.getYaw()).setPitch(angle.getPitch()).setRoll(angle.getRoll()))
                .setLeftEye(Point.newBuilder().setX(leftEye.x).setY(leftEye.y))
                .setRightEye(Point.newBuilder().setX(rightEye.x).setY(rightEye.y))
                .setQuality(faceQuality)
                .setSerialized(ByteString.copyFrom(faceData));
        for (PointF landmark : landmarks) {
            faceBuilder.addLandmarks(Point.newBuilder().setX(landmark.x).setY(landmark.y));
        }
        for (float dlibLandmark : dlibLandmarks) {
            faceBuilder.addDlibLandmarks(dlibLandmark);
        }
        return faceBuilder.build();
    }

    private Face createVerIDFace() {
        return new Face(bounds, angle, leftEye, rightEye, faceData, faceQuality, landmarks, dlibLandmarks);
    }

    private RecognizableFace createVerIDRecognizableFace() {
        byte[] data = new byte[176];
        for (int i=0; i<data.length; i++) {
            data[i] = (byte)i;
        }
        return new RecognizableFace(createVerIDFace(), data, VerIDFaceTemplateVersion.V20);
    }

    private com.appliedrec.verid.proto.RecognizableFace createProtoRecognizableFace() {
        byte[] data = new byte[176];
        for (int i=0; i<data.length; i++) {
            data[i] = (byte)i;
        }
        return com.appliedrec.verid.proto.RecognizableFace.newBuilder()
                .setFace(createProtoFace())
                .setTemplate(FaceTemplate.newBuilder().setData(ByteString.copyFrom(data)).setVersion(VerIDFaceTemplateVersion.V20.serialNumber(false)))
                .build();
    }

    private void compareImages(Image veridImage, com.appliedrec.verid.proto.Image protoImage) {
        assertEquals(veridImage.getWidth(), protoImage.getWidth());
        assertEquals(veridImage.getHeight(), protoImage.getHeight());
        assertEquals(veridImage.getBytesPerRow(), protoImage.getBytesPerRow());
        assertEquals(veridImage.getFormat().getName(), protoImage.getFormat().name());
        assertEquals(veridImage.getOrientation(), protoImage.getOrientation());
        assertArrayEquals(veridImage.getData(), protoImage.getData().toByteArray());
    }

    private void compareFaces(Face veridFace, com.appliedrec.verid.proto.Face protoFace) {
        float delta = 0.01f;
        assertEquals(veridFace.getBounds().left, protoFace.getBounds().getOrigin().getX(), delta);
        assertEquals(veridFace.getBounds().top, protoFace.getBounds().getOrigin().getY(), delta);
        assertEquals(veridFace.getBounds().width(), protoFace.getBounds().getSize().getWidth(), delta);
        assertEquals(veridFace.getBounds().height(), protoFace.getBounds().getSize().getHeight(), delta);
        assertEquals(veridFace.getAngle().getYaw(), protoFace.getAngle().getYaw(), delta);
        assertEquals(veridFace.getAngle().getPitch(), protoFace.getAngle().getPitch(), delta);
        assertEquals(veridFace.getAngle().getRoll(), protoFace.getAngle().getRoll(), delta);
        assertEquals(veridFace.getQuality(), protoFace.getQuality(), delta);
        assertArrayEquals(veridFace.getData(), protoFace.getSerialized().toByteArray());
        if (veridFace.getLandmarks() != null) {
            assertEquals(veridFace.getLandmarks().length, protoFace.getLandmarksCount());
            for (int i = 0; i < veridFace.getLandmarks().length; i++) {
                assertEquals(veridFace.getLandmarks()[i].x, protoFace.getLandmarks(i).getX(), delta);
                assertEquals(veridFace.getLandmarks()[i].y, protoFace.getLandmarks(i).getY(), delta);
            }
        }
        if (veridFace.getDlibLandmarks() != null && veridFace.getDlibLandmarks().length > 0) {
            assertEquals(veridFace.getDlibLandmarks().length, protoFace.getDlibLandmarksCount());
            for (int i = 0; i < veridFace.getDlibLandmarks().length; i++) {
                assertEquals(veridFace.getDlibLandmarks()[i], protoFace.getDlibLandmarks(i), delta);
            }
        }
        assertNotNull(veridFace.getLeftEye());
        assertNotNull(veridFace.getRightEye());
        assertEquals(veridFace.getLeftEye().x, protoFace.getLeftEye().getX(), delta);
        assertEquals(veridFace.getLeftEye().y, protoFace.getLeftEye().getY(), delta);
        assertEquals(veridFace.getRightEye().x, protoFace.getRightEye().getX(), delta);
        assertEquals(veridFace.getRightEye().y, protoFace.getRightEye().getY(), delta);
        assertEquals(veridFace.getAngle().getYaw(), protoFace.getAngle().getYaw(), delta);
        assertEquals(veridFace.getAngle().getPitch(), protoFace.getAngle().getPitch(), delta);
        assertEquals(veridFace.getAngle().getRoll(), protoFace.getAngle().getRoll(), delta);
    }

    private void compareRecognizableFaces(RecognizableFace veridFace, com.appliedrec.verid.proto.RecognizableFace protoFace) {
        compareFaces(veridFace, protoFace.getFace());
        assertEquals(veridFace.getVersion(), protoFace.getTemplate().getVersion());
        assertArrayEquals(veridFace.getRecognitionData(), protoFace.getTemplate().getData().toByteArray());
    }
}