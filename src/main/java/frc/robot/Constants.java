// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {public static final class Swerve{
    public static final double kDriveP = 2.432;
    public static final double kDriveI = 0.0;
    public static final double kDriveD = 0.0;
    public static final double kDriveS = 2.3289/10;
    public static final double kDriveV = 2.3289/10;
    public static final double kDriveA = 0.31447/10;
  
    public static final double kAngleP = 0.007;

    public static final double wheelCircumference = 2 * Math.PI * Units.inchesToMeters(2);

    public static final double kMaxSpeed = Units.feetToMeters(16.2); // 16.2 feet per second
    public static final double kMaxAngularSpeed = 2*Math.PI; // 1/2 rotation per second
    public static final double kModuleMaxAngularAcceleration = 2 * Math.PI; // radians per second squared
    
    public static final double kLength = 0.5903;
    public static final double kWidth = 0.5953;
    //public static final double kLength = 0.75;
    //public static final double kWidth = 0.75;

    public static final SwerveDriveKinematics kinematics =
        new SwerveDriveKinematics(
            new Translation2d(kLength / 2, kWidth / 2),
            new Translation2d(kLength / 2, -kWidth / 2),
            new Translation2d(-kLength / 2, kWidth / 2),
            new Translation2d(-kLength / 2, -kWidth / 2)
        );
        
    
     // Constraint for the motion profilied robot angle controller
     public static final TrapezoidProfile.Constraints kThetaControllerConstraints =
         new TrapezoidProfile.Constraints(
             kMaxAngularSpeed, kModuleMaxAngularAcceleration);
}

public static final boolean kGyroReversed = true;
}
