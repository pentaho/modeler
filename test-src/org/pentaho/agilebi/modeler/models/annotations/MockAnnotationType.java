package org.pentaho.agilebi.modeler.models.annotations;

import org.pentaho.agilebi.modeler.ModelerWorkspace;

/**
 * @author Rowell Belen
 */
public class MockAnnotationType extends AnnotationType {

  @ModelProperty( id = "i" )
  private int i;

  @ModelProperty( id = "d" )
  private double d;

  @ModelProperty( id = "f" )
  private float f;

  @ModelProperty( id = "l" )
  private long l;

  @ModelProperty( id = "s" )
  private short s;

  public int getI() {
    return i;
  }

  public void setI( int i ) {
    this.i = i;
  }

  public double getD() {
    return d;
  }

  public void setD( double d ) {
    this.d = d;
  }

  public float getF() {
    return f;
  }

  public void setF( float f ) {
    this.f = f;
  }

  public long getL() {
    return l;
  }

  public void setL( long l ) {
    this.l = l;
  }

  public short getS() {
    return s;
  }

  public void setS( short s ) {
    this.s = s;
  }

  @Override
  public boolean apply( ModelerWorkspace workspace, String column ) {
    return true;
  }

  @Override
  public ModelAnnotation.Type getType() {
    return null;
  }
}
