package io.github.wulkanowy.api.repository

import io.github.wulkanowy.api.register.Semester
import io.github.wulkanowy.api.service.StudentService
import io.github.wulkanowy.api.toLocalDate
import io.reactivex.Single
import org.threeten.bp.LocalDate.now

class StudentStartRepository(
    private val studentId: Int,
    private val api: StudentService
) {

    fun getSemesters(): Single<List<Semester>> {
        return api.getDiaries()
                .map { it.data?.filter { diary -> diary.studentId == studentId } }
                .map { diaries ->
                    diaries.map { diary ->
                        diary.semesters.reversed().map {
                            Semester(
                                    diaryId = diary.diaryId,
                                    diaryName = "${diary.level}${diary.symbol} ${diary.year}",
                                    semesterId = it.id,
                                    semesterNumber = it.number,
                                    start = it.start.toLocalDate(),
                                    end = it.end.toLocalDate(),
                                    current = it.start.toLocalDate() <= now() && it.end.toLocalDate() >= now()
                            )
                        }
                    }.flatten()
                }
    }
}
