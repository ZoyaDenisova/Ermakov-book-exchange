package org.bookswap.listings.mapper;

import org.bookswap.listings.dto.CityDto;
import org.bookswap.listings.entity.City;
import org.springframework.stereotype.Component;

@Component
public class CityMapper {

    public CityDto toDto(City city) {
        if (city == null) return null;

        return new CityDto(
                city.getId(),
                city.getName(),
                city.getRegion(),
                city.getCountry()
        );
    }
}