import React, { useMemo } from 'react';
import { Map, MapMarker, CustomOverlayMap } from 'react-kakao-maps-sdk';
import { useMentalMap } from '../../hooks/map/useMap';
import * as S from '../../style/pages/Map/Map.styles';

const fontAwesomeMarkerSvg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 384 512"><path fill="#FFC130" d="M0 188.6C0 84.4 86 0 192 0S384 84.4 384 188.6c0 119.3-120.2 262.3-170.4 316.8-11.8 12.8-31.5 12.8-43.3 0-50.2-54.5-170.4-197.5-170.4-316.8zM192 256a64 64 0 1 0 0-128 64 64 0 1 0 0 128z"/></svg>`;
const markerSrc = `data:image/svg+xml;base64,${btoa(fontAwesomeMarkerSvg)}`;

const MentalMap = () => {
    const {
        visibleInstitutions,
        updateMapBounds,
        categories,
        selectedCategories,
        toggleCategory,
        isFilterOpen,
        setIsFilterOpen,
        loading,
        error,
        mapCenter,
        mapRef,
        handleFindMyLocation,
        selectedInst,
        setSelectedInst,
        myLocation,
        keyword,
        suggestions,
        isDropdownOpen,
        handleInputChange,
        handleSuggestionClick,
        handleSearch,
        handleMapClick,
        formatDistance
    } = useMentalMap();

    const memoizedMarkers = useMemo(() => {
        return Array.isArray(visibleInstitutions) && visibleInstitutions.map((inst) => {
            if (inst.location && inst.location.coordinates) {
                return (
                    <MapMarker
                        key={inst.id || inst._id}
                        position={{
                            lat: inst.location.coordinates[1],
                            lng: inst.location.coordinates[0]
                        }}
                        title={inst.name || inst.NAME}
                        image={{
                            src: markerSrc,
                            size: { width: 32, height: 42 },
                            options: { offset: { x: 16, y: 42 } },
                        }}
                        onClick={() => {
                            setSelectedInst(inst);
                            if (mapRef.current) {
                                mapRef.current.panTo(new window.kakao.maps.LatLng(
                                    inst.location.coordinates[1],
                                    inst.location.coordinates[0]
                                ));
                            }
                        }}
                    />
                );
            }
            return null;
        });
    }, [visibleInstitutions, setSelectedInst, mapRef]);

    if (loading) return <S.LoadingErrorText>지도 스크립트 로딩 중...</S.LoadingErrorText>;
    if (error) return <S.LoadingErrorText $isError>지도를 불러오는데 실패했습니다.</S.LoadingErrorText>;

    return (
        <S.Container>
            <S.MapWrapper>

                <S.SearchWrapper>
                    <S.SearchContainer onSubmit={handleSearch}>
                        <S.SearchInput
                            type="text"
                            placeholder="동네, 지하철역, 장소 검색"
                            value={keyword}
                            onChange={handleInputChange}
                        />
                        <S.SearchButton type="submit">
                            <i className="fa-solid fa-magnifying-glass"></i>
                        </S.SearchButton>
                    </S.SearchContainer>

                    {isDropdownOpen && suggestions.length > 0 && (
                        <S.DropdownContainer>
                            {suggestions.map((inst) => (
                                <S.DropdownItem
                                    key={inst.id || inst._id}
                                    onClick={() => handleSuggestionClick(inst)}
                                >
                                    <S.DropdownItemHeader>
                                        <S.DropdownItemName>{inst.name || inst.NAME}</S.DropdownItemName>
                                        <S.DropdownItemDistance>{formatDistance(inst.distance)}</S.DropdownItemDistance>
                                    </S.DropdownItemHeader>
                                    <S.DropdownItemAddress>
                                        {inst.addr || inst.ADDR || "주소 정보 없음"}
                                    </S.DropdownItemAddress>
                                </S.DropdownItem>
                            ))}
                        </S.DropdownContainer>
                    )}
                </S.SearchWrapper>

                <S.ControlsContainer>
                    <S.FilterPanel $isOpen={isFilterOpen} onClick={(e) => e.stopPropagation()}>
                        {categories.map((category, index) => (
                            <S.FilterChip
                                key={index}
                                $isActive={category === '전체' ? selectedCategories.length === 0 : selectedCategories.includes(category)}
                                onClick={(e) => {
                                    e.stopPropagation();
                                    toggleCategory(category);
                                    setSelectedInst(null);
                                }}
                            >
                                {category}
                            </S.FilterChip>
                        ))}
                    </S.FilterPanel>

                    <S.ButtonsWrapper>
                        <S.FilterToggleButton onClick={(e) => {
                            e.stopPropagation();
                            setIsFilterOpen(!isFilterOpen);
                        }}>
                            <i className="fa-solid fa-filter"></i>
                        </S.FilterToggleButton>

                        <S.MyLocationButton onClick={(e) => {
                            e.stopPropagation();
                            handleFindMyLocation();
                        }}>
                            <i className="fa-solid fa-location-crosshairs"></i>
                        </S.MyLocationButton>
                    </S.ButtonsWrapper>
                </S.ControlsContainer>

                <Map
                    center={mapCenter}
                    ref={mapRef}
                    style={{ width: "100%", height: "100%" }}
                    level={3}
                    isPanto={true}
                    onClick={handleMapClick}
                    onCreate={updateMapBounds}
                    onIdle={updateMapBounds}
                >
                    {memoizedMarkers}

                    {selectedInst && selectedInst.location && (
                        <CustomOverlayMap
                            position={{
                                lat: selectedInst.location.coordinates[1],
                                lng: selectedInst.location.coordinates[0]
                            }}
                            clickable={true}
                            zIndex={15}
                        >
                            <S.OverlayContainer>

                                <S.OverlayLeftSection>
                                    <S.OverlayHeader>
                                        <div>
                                            <S.OverlayTitle>{selectedInst.name || selectedInst.NAME}</S.OverlayTitle>
                                        </div>
                                    </S.OverlayHeader>
                                    <S.OverlayBody>
                                        <S.InfoText>
                                            <i className="fa-solid fa-location-dot"></i>
                                            <span>{selectedInst.addr || selectedInst.ADDR || "주소 정보 없음"}</span>
                                        </S.InfoText>

                                        {(selectedInst.homepage || selectedInst.HOMEPAGE) && (
                                            <S.InfoText>
                                                <i className="fa-solid fa-globe"></i>
                                                <a href={selectedInst.homepage || selectedInst.HOMEPAGE} target="_blank"
                                                   rel="noopener noreferrer">
                                                    {selectedInst.homepage || selectedInst.HOMEPAGE}
                                                </a>
                                            </S.InfoText>
                                        )}

                                        {(selectedInst.category || selectedInst.CATEGORY) && (
                                            <S.HashtagText>#{selectedInst.category || selectedInst.CATEGORY}</S.HashtagText>
                                        )}
                                    </S.OverlayBody>
                                </S.OverlayLeftSection>

                                <S.OverlayRightSection>
                                    <S.RouteButtonRound
                                        href={
                                            myLocation
                                                ? `https://map.kakao.com/link/from/내위치,${myLocation.lat},${myLocation.lng}/to/${selectedInst.name || selectedInst.NAME},${selectedInst.location.coordinates[1]},${selectedInst.location.coordinates[0]}`
                                                : `https://map.kakao.com/link/to/${selectedInst.name || selectedInst.NAME},${selectedInst.location.coordinates[1]},${selectedInst.location.coordinates[0]}`
                                        }
                                        target="_blank"
                                        rel="noopener noreferrer"
                                    >
                                        <i className="fa-solid fa-compass"></i>
                                    </S.RouteButtonRound>
                                </S.OverlayRightSection>

                            </S.OverlayContainer>
                        </CustomOverlayMap>
                    )}
                </Map>
            </S.MapWrapper>
        </S.Container>
    );
};

export default MentalMap;