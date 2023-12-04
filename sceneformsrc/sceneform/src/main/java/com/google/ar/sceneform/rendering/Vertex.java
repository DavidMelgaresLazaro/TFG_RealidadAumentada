package com.google.ar.sceneform.rendering;

//change to androidx
import androidx.annotation.Nullable;


import com.google.ar.sceneform.math.Vector3;

/**
 * Represents a Vertex for a {@link RenderableDefinition}. Used for constructing renderables
 * dynamically.
 *
 * @see ModelRenderable.Builder
 * @see ViewRenderable.Builder
 */
public class Vertex {
  /** Represents a texture Coordinate for a Vertex. Values should be between 0 and 1. */
  public static final class UvCoordinate {
    public float x;
    public float y;

    public UvCoordinate(float x, float y) {
      this.x = x;
      this.y = y;
    }
  }

  // Required.
  private final Vector3 position = Vector3.zero();

  // Optional.
  @Nullable private Vector3 normal;
  @androidx.annotation.Nullable
  private UvCoordinate uvCoordinate;
  @androidx.annotation.Nullable
  private Color color;

  public void setPosition(Vector3 position) {
    this.position.set(position);
  }

  public Vector3 getPosition() {
    return position;
  }

  public void setNormal(@androidx.annotation.Nullable Vector3 normal) {
    this.normal = normal;
  }

  @androidx.annotation.Nullable
  public Vector3 getNormal() {
    return normal;
  }

  public void setUvCoordinate(@androidx.annotation.Nullable UvCoordinate uvCoordinate) {
    this.uvCoordinate = uvCoordinate;
  }

  @androidx.annotation.Nullable
  public UvCoordinate getUvCoordinate() {
    return uvCoordinate;
  }

  public void setColor(@androidx.annotation.Nullable Color color) {
    this.color = color;
  }

  @androidx.annotation.Nullable
  public Color getColor() {
    return color;
  }

  private Vertex(Builder builder) {
    position.set(builder.position);
    normal = builder.normal;
    uvCoordinate = builder.uvCoordinate;
    color = builder.color;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Factory class for {@link Vertex}. */
  public static final class Builder {
    // Required.
    private final Vector3 position = Vector3.zero();

    // Optional.
    @androidx.annotation.Nullable
    private Vector3 normal;
    @androidx.annotation.Nullable
    private UvCoordinate uvCoordinate;
    @androidx.annotation.Nullable
    private Color color;

    public Builder setPosition(Vector3 position) {
      this.position.set(position);
      return this;
    }

    public Builder setNormal(@androidx.annotation.Nullable Vector3 normal) {
      this.normal = normal;
      return this;
    }

    public Builder setUvCoordinate(@androidx.annotation.Nullable UvCoordinate uvCoordinate) {
      this.uvCoordinate = uvCoordinate;
      return this;
    }

    public Builder setColor(@androidx.annotation.Nullable Color color) {
      this.color = color;
      return this;
    }

    public Vertex build() {
      return new Vertex(this);
    }
  }
}
