package XiGyoku.furyborn.client.entity;

import XiGyoku.furyborn.client.util.PhotonRenderUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DriveshiftParticleRenderer {

    private static final List<Photon> PHOTONS = new ArrayList<>();
    private static final RandomSource RANDOM = RandomSource.create();

    public static class Photon {
        public boolean isOrbital;
        public boolean isWavy;
        public Vector3f startPos;
        public Vector3f endPos;
        public Vector3f center;
        public Vector3f right;
        public Vector3f up;
        public float radius;
        public float currentAngle;
        public float angularVelocity;
        public Vector3f currentPos;
        public float progress;
        public float speed;
        public float brightness;
        public Vector4f startColor;
        public Vector4f endColor;
        public float size;
        public float fadeFraction = 0.15F;

        public float waveAmplitude;
        public float waveFrequency;
        public float wavePhase;
        public Vector3f waveAxis;

        public Photon(Vector3f startPos, Vector3f endPos, float speed, float brightness, Vector4f startColor, Vector4f endColor, float size) {
            this.isOrbital = false;
            this.isWavy = false;
            this.startPos = startPos;
            this.endPos = endPos;
            this.currentPos = new Vector3f(startPos);
            this.progress = 0.0F;
            this.speed = speed;
            this.brightness = brightness;
            this.startColor = startColor;
            this.endColor = endColor;
            this.size = size;
        }

        public Photon(Vector3f center, Vector3f right, Vector3f up, float radius, float startAngle, float angularVelocity, float speed, float brightness, Vector4f startColor, Vector4f endColor, float size) {
            this.isOrbital = true;
            this.isWavy = false;
            this.center = center;
            this.right = right;
            this.up = up;
            this.radius = radius;
            this.currentAngle = startAngle;
            this.angularVelocity = angularVelocity;
            this.progress = 0.0F;
            this.speed = speed;
            this.brightness = brightness;
            this.startColor = startColor;
            this.endColor = endColor;
            this.size = size;
            updateOrbitalPosition();
        }

        public void setWavy(float amplitude, float frequency, Vector3f axis, float phase) {
            this.isWavy = true;
            this.waveAmplitude = amplitude;
            this.waveFrequency = frequency;
            this.waveAxis = axis;
            this.wavePhase = phase;
        }

        private void updateOrbitalPosition() {
            float c = Mth.cos(this.currentAngle) * this.radius;
            float s = Mth.sin(this.currentAngle) * this.radius;
            float x = this.center.x() + this.right.x() * c + this.up.x() * s;
            float y = this.center.y() + this.right.y() * c + this.up.y() * s;
            float z = this.center.z() + this.right.z() * c + this.up.z() * s;
            this.currentPos = new Vector3f(x, y, z);
        }

        public boolean update() {
            this.progress += this.speed;
            if (this.progress > 1.0F) {
                return false;
            }
            if (this.isOrbital) {
                this.currentAngle += this.angularVelocity;
                updateOrbitalPosition();
            } else {
                Vector3f basePos = PhotonRenderUtil.lerpVector(this.startPos, this.endPos, this.progress);
                if (this.isWavy) {
                    float waveOffset = this.waveAmplitude * Mth.sin(this.progress * this.waveFrequency + this.wavePhase);
                    this.currentPos = new Vector3f(
                            basePos.x() + this.waveAxis.x() * waveOffset,
                            basePos.y() + this.waveAxis.y() * waveOffset,
                            basePos.z() + this.waveAxis.z() * waveOffset
                    );
                } else {
                    this.currentPos = basePos;
                }
            }
            return true;
        }
    }

    public static void spawnLineToLine(Vector3f line1Start, Vector3f line1End, Vector3f line2Start, Vector3f line2End, int count, float speed, float minBright, float maxBright, Vector4f startColor, Vector4f endColor, float size) {
        spawnLineToLineWavy(line1Start, line1End, line2Start, line2End, count, speed, minBright, maxBright, startColor, endColor, size, false, 0.0F, 0.0F);
    }

    public static void spawnLineToLineWavy(Vector3f line1Start, Vector3f line1End, Vector3f line2Start, Vector3f line2End, int count, float speed, float minBright, float maxBright, Vector4f startColor, Vector4f endColor, float size, boolean isWavy, float waveAmp, float waveFreq) {
        for (int i = 0; i < count; i++) {
            float ratio = RANDOM.nextFloat();
            Vector3f start = PhotonRenderUtil.lerpVector(line1Start, line1End, ratio);
            Vector3f end = PhotonRenderUtil.lerpVector(line2Start, line2End, ratio);
            float brightness = Mth.lerp(RANDOM.nextFloat(), minBright, maxBright);
            Photon photon = new Photon(start, end, speed, brightness, startColor, endColor, size);

            if (isWavy) {
                Vector3f forward = new Vector3f(end.x() - start.x(), end.y() - start.y(), end.z() - start.z());
                if (forward.lengthSquared() > 1e-5f) {
                    forward.normalize();
                } else {
                    forward.set(0, 1, 0);
                }

                Vector3f up = new Vector3f(0, 1, 0);
                Vector3f right = new Vector3f();
                forward.cross(up, right);

                Vector3f waveAxis = new Vector3f();
                if (right.lengthSquared() < 1e-5f) {
                    waveAxis.set(1, 0, 0);
                } else {
                    right.normalize();
                    right.cross(forward, waveAxis);
                    waveAxis.normalize();
                }

                float phase = (start.x() * 13.37f + start.y() * 42.0f + start.z() * 9.99f);
                photon.setWavy(waveAmp, waveFreq, waveAxis, phase);
            }

            PHOTONS.add(photon);
        }
    }

    public static void spawnOrientedOrbitalRing(Matrix4f pose, Vector3f worldOffset, Vector3f localCenter, float radius, float ringThickness, int count, float lifeSpeed, float angularVelocity, float minBright, float maxBright, Vector4f startColor, Vector4f endColor, float size) {
        Vector3f centerWorld = new Vector3f(localCenter);
        pose.transformPosition(centerWorld);
        centerWorld.add(worldOffset);

        Vector3f right = new Vector3f(1.0F, 0.0F, 0.0F);
        pose.transformDirection(right);
        right.normalize();

        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
        pose.transformDirection(up);
        up.normalize();

        for (int i = 0; i < count; i++) {
            float angle = RANDOM.nextFloat() * Mth.TWO_PI;
            float r = radius + (RANDOM.nextFloat() - 0.5F) * ringThickness;
            float brightness = Mth.lerp(RANDOM.nextFloat(), minBright, maxBright);
            PHOTONS.add(new Photon(centerWorld, right, up, r, angle, angularVelocity, lifeSpeed, brightness, startColor, endColor, size));
        }
    }

    public static void spawnOrientedCircleToCircleWave(Matrix4f pose, Vector3f worldOffset, Vector3f localCenter1, float radius1, Vector3f localCenter2, float radius2, int count, float speed, float minBright, float maxBright, Vector4f startColor, Vector4f endColor, float size) {
        float angleStep = Mth.TWO_PI / count;

        Vector3f right = new Vector3f(1.0F, 0.0F, 0.0F);
        pose.transformDirection(right);
        right.normalize();

        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
        pose.transformDirection(up);
        up.normalize();

        Vector3f center1World = new Vector3f(localCenter1);
        pose.transformPosition(center1World);
        center1World.add(worldOffset);

        Vector3f center2World = new Vector3f(localCenter2);
        pose.transformPosition(center2World);
        center2World.add(worldOffset);

        for (int i = 0; i < count; i++) {
            float angle = i * angleStep;
            float c = Mth.cos(angle);
            float s = Mth.sin(angle);

            Vector3f start = new Vector3f(
                    center1World.x() + right.x() * c * radius1 + up.x() * s * radius1,
                    center1World.y() + right.y() * c * radius1 + up.y() * s * radius1,
                    center1World.z() + right.z() * c * radius1 + up.z() * s * radius1
            );
            Vector3f end = new Vector3f(
                    center2World.x() + right.x() * c * radius2 + up.x() * s * radius2,
                    center2World.y() + right.y() * c * radius2 + up.y() * s * radius2,
                    center2World.z() + right.z() * c * radius2 + up.z() * s * radius2
            );
            float brightness = Mth.lerp(RANDOM.nextFloat(), minBright, maxBright);
            PHOTONS.add(new Photon(start, end, speed, brightness, startColor, endColor, size));
        }
    }

    public static void spawnOrientedCone(Matrix4f pose, Vector3f worldOffset, Vector3f localStart, float distance, int count, float speed, float minBright, float maxBright, Vector4f startColor, Vector4f endColor, float size) {
        Vector3f startWorld = new Vector3f(localStart);
        pose.transformPosition(startWorld);
        startWorld.add(worldOffset);

        float cosMax = Mth.cos(15.0F * Mth.DEG_TO_RAD);

        for (int i = 0; i < count; i++) {
            float z = Mth.lerp(RANDOM.nextFloat(), cosMax, 1.0F);
            float phi = RANDOM.nextFloat() * Mth.TWO_PI;
            float sinTheta = Mth.sqrt(1.0F - z * z);
            float x = sinTheta * Mth.cos(phi);
            float y = sinTheta * Mth.sin(phi);

            Vector3f localEnd = new Vector3f(localStart.x() + x * distance, localStart.y() + y * distance, localStart.z() - z * distance);
            Vector3f endWorld = new Vector3f(localEnd);
            pose.transformPosition(endWorld);
            endWorld.add(worldOffset);

            float brightness = Mth.lerp(RANDOM.nextFloat(), minBright, maxBright);
            Photon photon = new Photon(startWorld, endWorld, speed, brightness, startColor, endColor, size);
            photon.fadeFraction = 0.5F;
            PHOTONS.add(photon);
        }
    }

    public static void tick() {
        Iterator<Photon> iterator = PHOTONS.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().update()) {
                iterator.remove();
            }
        }
    }

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        if (PHOTONS.isEmpty()) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        VertexConsumer consumer = bufferSource.getBuffer(PhotonRenderUtil.GLOWING_PHOTON);

        for (Photon photon : PHOTONS) {
            float fade = PhotonRenderUtil.calculateFade(photon.progress, photon.fadeFraction);
            Vector4f currentColor = PhotonRenderUtil.lerpColor(photon.startColor, photon.endColor, photon.progress);
            PhotonRenderUtil.drawPhoton(poseStack, consumer, photon.currentPos, photon.size, currentColor, photon.brightness * fade);
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    public static void clear() {
        PHOTONS.clear();
    }
}