import React from 'react';
import { Map, MapMarker } from 'react-kakao-maps-sdk';
import { useMentalMap } from '../../hooks/map/useMap';
import * as S from '../../style/pages/Map/Map.styles';

const MentalMap = () => {
    const { institutions, loading, error } = useMentalMap();

    if (loading) return <S.LoadingErrorText>지도 스크립트 로딩 중...</S.LoadingErrorText>;
    if (error) return <S.LoadingErrorText $isError>지도를 불러오는데 실패했습니다.</S.LoadingErrorText>;

    return (
        <S.Container>

            <S.MapWrapper>
                <Map
                    center={{ lat: 37.5665, lng: 126.9780 }}
                    style={{ width: "100%", height: "100%" }}
                    level={7}
                >
                    {institutions.map((inst) => {
                        if (inst.location && inst.location.coordinates) {
                            return (
                                <MapMarker
                                    key={inst.id}
                                    position={{
                                        lat: inst.location.coordinates[1],
                                        lng: inst.location.coordinates[0]
                                    }}
                                    title={inst.name}
                                />
                            );
                        }
                        return null;
                    })}
                </Map>
            </S.MapWrapper>
        </S.Container>
    );
};

export default MentalMap;