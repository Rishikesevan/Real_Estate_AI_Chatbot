package com.project.RealEstate.Repository;


import com.project.RealEstate.Entity.NewProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewProjectRepository extends JpaRepository<NewProject,Long>
{

}
