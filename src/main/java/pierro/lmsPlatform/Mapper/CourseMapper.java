package pierro.lmsPlatform.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pierro.lmsPlatform.DTO.Request.Learning.CreateCourseDto;
import pierro.lmsPlatform.DTO.Response.Learning.CourseDetailDto;
import pierro.lmsPlatform.DTO.Response.Learning.CourseSummaryDto;
import pierro.lmsPlatform.Entity.Learning.Course;

import java.util.List;

@Mapper(componentModel = "spring", uses = {IdMapper.class, LessonMapper.class})
public interface CourseMapper {
    CourseSummaryDto toSummary(Course course);

    List<CourseSummaryDto> toSummaries(List<Course> courses);

    CourseDetailDto toDetail(Course course);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    Course toEntity(CreateCourseDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    void updateEntity(CreateCourseDto dto, @MappingTarget Course course);
}
