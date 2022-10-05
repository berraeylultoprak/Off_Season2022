// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Shooter extends SubsystemBase {
  /** Creates a new Shooter. */

  public WPI_TalonFX masterMotor = new WPI_TalonFX(0); 
  public WPI_TalonFX slaveMotor = new WPI_TalonFX(1); 
  public Shooter() {}

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}
