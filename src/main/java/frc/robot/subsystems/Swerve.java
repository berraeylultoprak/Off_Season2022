// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class Swerve extends SubsystemBase {

  /*
  TODO realtime PID tuning - look into docs
  TODO look into feedforward tuning

  */

  private boolean isCalibrating;
  private boolean offsetCalibration = true;
  private boolean driveCalibration = false;
  private boolean rotCalibration = true;

  private Rotation2d fieldAngle = new Rotation2d();

  private final Field2d field2D = new Field2d();
  
  /**
   * TODO: These are example values and will need to be adjusted for your robot!
   * Modules are in the order of -
   * Front Left
   * Front Right
   * Back Left
   * Back Right
   * 
   * Positive x values represent moving toward the front of the robot whereas
   * positive y values represent moving toward the left of the robot
   * https://docs.wpilib.org/en/stable/docs/software/kinematics-and-odometry/swerve-drive-kinematics.html#constructing-the-kinematics-object
   */

  //private final CKIMU gyro;
  private AHRS gyroAhrs = new AHRS();

  TalonFX driveMotorBL = new TalonFX(11);

  // TODO: Update these CAN device IDs to match your TalonFX + CANCoder device IDs | Done
  // TODO: Update module offsets to match your CANCoder offsets | Done

  private double[] pidValues = {
    0.09698,
    0.09698,
    0.09698,
    0.09698
  };


  final boolean invertAllModules = false;
  private SwerveModule[] modules = new SwerveModule[] {
    new SwerveModule("FL", new TalonFX(17), new TalonFX(13), new DutyCycleEncoder( new DigitalInput(0)), Rotation2d.fromDegrees(-27), true^invertAllModules, new PIDController(pidValues[0], 0, 0)), //! Front Left
    new SwerveModule("FR", new TalonFX(14), new TalonFX(15), new DutyCycleEncoder( new DigitalInput(2)), Rotation2d.fromDegrees(-128), true^invertAllModules, new PIDController(pidValues[1], 0, 0)), //! Front Right
    new SwerveModule("RL", driveMotorBL, new TalonFX(16), new DutyCycleEncoder(new DigitalInput(1)), Rotation2d.fromDegrees(54), 
    false^invertAllModules, new PIDController(pidValues[2], 0, 0)), //! Back Left
    new SwerveModule("RR", new TalonFX(10), new TalonFX(12), new DutyCycleEncoder( new DigitalInput(3) ), Rotation2d.fromDegrees(-103), true^invertAllModules, new PIDController(pidValues[3], 0, 0))  //! Back Right
  };

  public Swerve(boolean isCalibrating) {
    driveMotorBL.setInverted(true);
    this.isCalibrating = isCalibrating;
    resetAllEncoders();
    
    new Thread(() -> {
      try {
        Thread.sleep(1000);
        gyroAhrs.reset();
      } catch (Exception e) {
        //TODO: handle exception
      }
    }).start();
  
    SmartDashboard.putData("Field", field2D);
  }
  
  public Rotation2d getGyro(){
    return Rotation2d.fromDegrees(
        getGyroDouble()
        );
  }

  public double getGyroDouble(){
    return Math.IEEEremainder(gyroAhrs.getAngle(), 360.0) * (Constants.kGyroReversed ? -1.0 : 1.0);
  }

  SwerveDriveOdometry odometry = new SwerveDriveOdometry(
    Constants.Swerve.kinematics,
    getGyro()
  );
  private Rotation2d teleopAngle = new Rotation2d(0);

  public void stopModules(){
    modules[0].stopMotors();
    modules[1].stopMotors();
    modules[2].stopMotors();
    modules[3].stopMotors();
  }
  

  public void resetAllEncoders(){
    for (int i = modules.length-1; i >= 0; i--) {
      SwerveModule module = modules[i];
      //module.resetRotationEncoder();
      module.resetDriveEncoder();
    }
  }

  public double getAverageDistance(){
    double sum = 0;
    for (int i = modules.length-1; i >= 0; i--) {
      SwerveModule module = modules[i];
      sum += module.getPosition();
    }
    return sum / modules.length;
  }

  public Pose2d getPose(){
    return odometry.getPoseMeters();
  }

  public void resetOdometry(Pose2d pose) {
    odometry.resetPosition(
      pose, 
      getGyro()
    );
    // resetAllEncoders();
  }

  public void drive(double xSpeed, double ySpeed, double rot, boolean fieldRelative) {
    SwerveModuleState[] states =
    Constants.Swerve.kinematics.toSwerveModuleStates(
        fieldRelative
          ? ChassisSpeeds.fromFieldRelativeSpeeds(xSpeed, ySpeed, rot, getGyro().plus(fieldAngle).minus(teleopAngle))
          : new ChassisSpeeds(xSpeed, ySpeed, rot));
    SwerveDriveKinematics.desaturateWheelSpeeds(states, Constants.Swerve.kMaxSpeed);
    //setClosedLoopStates(states);
    
    for (int i = 0; i < states.length; i++) {
      SwerveModule module = modules[i];
      SwerveModuleState state = states[i];
      module.setDesiredState(state);
    }

    
  }

  public void resetFieldOrientation() {
    resetFieldOrientation(new Rotation2d(getGyroDouble()));
  }
  public void resetFieldOrientation(Rotation2d angle) {
    this.fieldAngle = angle;
  }

  public void resetFieldOrientedTeleOp(){
    this.teleopAngle = getGyro();
  }

  public void setModuleStates(SwerveModuleState[] desiredStates) {
    SwerveDriveKinematics.desaturateWheelSpeeds(
        desiredStates, Constants.Swerve.kMaxSpeed);
    modules[0].setDesiredState(desiredStates[0]);
    modules[1].setDesiredState(desiredStates[1]);
    modules[2].setDesiredState(desiredStates[2]);
    modules[3].setDesiredState(desiredStates[3]);
  }

  public void setClosedLoopStates(SwerveModuleState[] desiredStates) {

    SwerveDriveKinematics.desaturateWheelSpeeds(
        desiredStates, Constants.Swerve.kMaxSpeed);
    modules[0].setClosedLoop(desiredStates[0]);
    modules[1].setClosedLoop(desiredStates[1]);
    modules[2].setClosedLoop(desiredStates[2]);
    modules[3].setClosedLoop(desiredStates[3]);
  }

  @Override
  public void periodic() {
    SmartDashboard.putNumber("groAngle", getGyroDouble());
    SmartDashboard.putNumber("field offset", fieldAngle.getDegrees());
    SmartDashboard.putNumber("bozukModuk", modules[2].getAngle().getDegrees());

    SmartDashboard.putNumber("bozukdegil", modules[3].getAngle().getDegrees());

    // SmartDashboard.putNumber("0. SETPOINT", modules[0].drivePID.getSetpoint());
    // /SmartDashboard.putNumber("0. Velocity", modules[0].getDriveMotorRate());

    // SmartDashboard.putNumber("1. SETPOINT", modules[1].drivePID.getSetpoint());
    // SmartDashboard.putNumber("1. Velocity", modules[1].getDriveMotorRate());

    // SmartDashboard.putNumber("2. SETPOINT", modules[2].drivePID.getSetpoint());
    // SmartDashboard.putNumber("2. Velocity", modules[2].getDriveMotorRate());

    // SmartDashboard.putNumber("3. SETPOINT", modules[3].drivePID.getSetpoint());
    // SmartDashboard.putNumber("3. Velocity", modules[3].getDriveMotorRate());
    
    /*
    SmartDashboard.putNumber("1. modül", modules[1].getDriveMotorRate());
    SmartDashboard.putNumber("2. modül", modules[2].getDriveMotorRate());
    SmartDashboard.putNumber("3. modül", modules[3].getDriveMotorRate());
    SmartDashboard.putNumber("average Distance", getAverageDistance());
    */
    SmartDashboard.putNumber("Posex", getPose().getX());
    SmartDashboard.putNumber("Posey", getPose().getY());
    SmartDashboard.putNumber("Rot", getPose().getRotation().getRadians());

    SwerveModuleState[] moduleStates = {
      modules[0].getState(),
      modules[1].getState(),
      modules[2].getState(),
      modules[3].getState()
    };
    
    modules[0].debug();
    modules[1].debug();
    modules[2].debug();
    modules[3].debug();

    // moduleStates[0].speedMetersPerSecond = Math.abs(modules[0].getDriveEncoderVelocity());
    // moduleStates[1].speedMetersPerSecond = Math.abs(modules[1].getDriveEncoderVelocity());
    // moduleStates[2].speedMetersPerSecond = Math.abs(modules[2].getDriveEncoderVelocity());
    // moduleStates[3].speedMetersPerSecond = Math.abs(modules[3].getDriveEncoderVelocity());

    odometry.update(
      getGyro(),
      moduleStates[0],
      moduleStates[1],
      moduleStates[2],
      moduleStates[3]
      );
      

    field2D.setRobotPose(getPose());

    if(isCalibrating){
      modules[0].calibrate("Front Left", offsetCalibration, driveCalibration, rotCalibration);
      modules[1].calibrate("Front Right", offsetCalibration, driveCalibration, rotCalibration);
      modules[2].calibrate("Back Left", offsetCalibration, driveCalibration, rotCalibration);
      modules[3].calibrate("Back Right", offsetCalibration, driveCalibration, rotCalibration);
    }

  }


  public void addTrajectoryToField2d(Trajectory traj) {
    field2D.getObject("traj").setTrajectory(traj);
  }

}