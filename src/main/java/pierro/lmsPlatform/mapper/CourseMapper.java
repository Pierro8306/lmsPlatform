package pierro.lmsPlatform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pierro.lmsPlatform.dto.request.Learning.CreateCourseDto;
import pierro.lmsPlatform.dto.response.Learning.CourseDetailDto;
import pierro.lmsPlatform.dto.response.Learning.CourseSummaryDto;
import pierro.lmsPlatform.entity.learning.Course;

import java.util.List;

@Mapper(componentModel = "spring", uses = {IdMapper.class, LessonMapper.class})
public interface CourseMapper {
    @Mapping(source = "userGroup.name", target = "userGroup")
    @Mapping(target = "totalLessons", ignore = true)
    @Mapping(target = "completedLessons", ignore = true)
    @Mapping(target = "progressPercent", ignore = true)
    CourseSummaryDto toSummary(Course course);

    List<CourseSummaryDto> toSummaries(List<Course> courses);

    @Mapping(source = "userGroup.name", target = "userGroup")
    @Mapping(target = "totalLessons", ignore = true)
    @Mapping(target = "completedLessons", ignore = true)
    @Mapping(target = "progressPercent", ignore = true)
    CourseDetailDto toDetail(Course course);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "userGroup", ignore = true)
    Course toEntity(CreateCourseDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "userGroup", ignore = true)
    void updateEntity(CreateCourseDto dto, @MappingTarget Course course);
}
