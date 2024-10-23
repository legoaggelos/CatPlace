package com.legoaggelos.catplace;

import org.springframework.data.annotation.Id;

record Cat(@Id Long id, String name, Integer ageInMonths, String owner) {

}
