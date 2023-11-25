package com.google.ar.sceneform.rendering;

import android.content.Context;
import android.net.Uri;

//change to androidx
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.google.android.filament.IndexBuffer;
import com.google.android.filament.VertexBuffer;
import com.google.android.filament.gltfio.MaterialProvider;
import com.google.android.filament.gltfio.ResourceLoader;


import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.RenderableInternalData.MeshData;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import java.util.List;

import java.util.function.Function;

/** Represents the data used by a {@link Renderable} for rendering natively loaded glTF data. */
@SuppressWarnings("AndroidJdkLibsChecker")
public class RenderableInternalFilamentAssetData implements IRenderableInternalData {

  Context context;
  Buffer gltfByteBuffer;
  boolean isGltfBinary;
  ResourceLoader resourceLoader;
  @androidx.annotation.Nullable
  Function<String, Uri> urlResolver;
  static MaterialProvider materialProvider;

  static MaterialProvider getMaterialProvider() {
    if (materialProvider == null) {
      materialProvider = new MaterialProvider(EngineInstance.getEngine().getFilamentEngine());
    }
    return materialProvider;
  }

  @Override
  public void setCenterAabb(Vector3 center) {
    // Not Implemented
  }

  @Override
  public Vector3 getCenterAabb() {
    // Not Implemented
    return Vector3.zero();
  }

  @Override
  public void setExtentsAabb(Vector3 halfExtents) {
    // Not Implemented
  }

  @Override
  public Vector3 getExtentsAabb() {
    throw new IllegalStateException("Not Implemented");
  }

  @Override
  public Vector3 getSizeAabb() {
    // Not Implemented
    return Vector3.zero();
  }

  @Override
  public void setTransformScale(float scale) {
    // Not Implemented
  }

  @Override
  public float getTransformScale() {
    // Not Implemented
    return 1.0f;
  }

  @Override
  public void setTransformOffset(Vector3 offset) {
    // Not Implemented
  }

  @Override
  public Vector3 getTransformOffset() {
    // Not Implemented
    return Vector3.zero();
  }

  @Override
  public ArrayList<MeshData> getMeshes() {
    // Not Implemented
    return new ArrayList<>();
  }

  public ArrayList<Integer> getMaterialBindingIds() {
    // Not Implemented
    return new ArrayList<>();
  }

  @Override
  public void setIndexBuffer(@androidx.annotation.Nullable IndexBuffer indexBuffer) {
    // Not Implemented
  }

  @androidx.annotation.Nullable
  @Override
  public IndexBuffer getIndexBuffer() {
    // Not Implemented
    return null;
  }

  @Override
  public void setVertexBuffer(@androidx.annotation.Nullable VertexBuffer vertexBuffer) {
    // Not Implemented
  }

  @androidx.annotation.Nullable
  @Override
  public VertexBuffer getVertexBuffer() {
    // Not Implemented
    return null;
  }

  @Override
  public void setRawIndexBuffer(@androidx.annotation.Nullable IntBuffer rawIndexBuffer) {
    // Not Implemented
  }

  @androidx.annotation.Nullable
  @Override
  public IntBuffer getRawIndexBuffer() {
    // Not Implemented
    return null;
  }

  @Override
  public void setRawPositionBuffer(@androidx.annotation.Nullable FloatBuffer rawPositionBuffer) {
    // Not Implemented
  }

  @androidx.annotation.Nullable
  @Override
  public FloatBuffer getRawPositionBuffer() {
    // Not Implemented
    return null;
  }

  @Override
  public void setRawTangentsBuffer(@androidx.annotation.Nullable FloatBuffer rawTangentsBuffer) {
    // Not Implemented
  }

  @androidx.annotation.Nullable
  @Override
  public FloatBuffer getRawTangentsBuffer() {
    // Not Implemented
    return null;
  }

  @Override
  public void setRawUvBuffer(@androidx.annotation.Nullable FloatBuffer rawUvBuffer) {
    // Not Implemented
  }

  @androidx.annotation.Nullable
  @Override
  public FloatBuffer getRawUvBuffer() {
    // Not Implemented
    return null;
  }

  @Override
  public void setRawColorBuffer(@androidx.annotation.Nullable FloatBuffer rawColorBuffer) {
    // Not Implemented
  }

  @androidx.annotation.Nullable
  @Override
  public FloatBuffer getRawColorBuffer() {
    // Not Implemented
    return null;
  }

  @Override
  public void setAnimationNames(@androidx.annotation.NonNull List<String> animationNames) {
    // Not Implemented
  }

  @Override
  @androidx.annotation.NonNull
  public List<String> getAnimationNames() {
    // Not Implemented
    return new ArrayList<>();
  }

  





  







  





  @Override
  public void buildInstanceData(Renderable renderable, int renderedEntity) {}

  @Override
  public void dispose() {}
}
