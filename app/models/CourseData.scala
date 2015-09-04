package models

import java.sql.Timestamp

import models.Tables._

/**
 * @author mlewis
 */
case class NewCourseData(code: String, semester: String, section: Int, instructorNames: String, studentData: String)

case class StudentData(userId: String, username: String, correct: Int)

case class CourseQuizData(row: QuizzesRow, time: Timestamp)

case class CourseData(row: CoursesRow, instructors: Seq[UsersRow], students: Seq[StudentData], quizzes: Seq[CourseQuizData], totalQuestions: Int)