package models.redux

import java.time.LocalDateTime

case class UserQuizInfo(id: Int, name: String, description: String,
                        openTime: LocalDateTime, closeTime: LocalDateTime,
                        score: (Int, Int))