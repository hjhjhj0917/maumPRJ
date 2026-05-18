import { useState, useEffect, useRef } from 'react';
import { useKakaoLoader } from 'react-kakao-maps-sdk';
import { getInstitutions } from '../../api/mapApi';

export const useMentalMap = () => {
    const [institutions, setInstitutions] = useState([]);
    const [mapCenter, setMapCenter] = useState({ lat: 37.5665, lng: 126.9780 });
    const [selectedInst, setSelectedInst] = useState(null);
    const [myLocation, setMyLocation] = useState(null);
    const mapRef = useRef(null);

    const [loading, error] = useKakaoLoader({
        appkey: import.meta.env.VITE_KAKAO_JS_KEY,
        libraries: ["clusterer", "services"]
    });

    useEffect(() => {
        const fetchInstitutions = async () => {
            try {
                const response = await getInstitutions();
                const realData = response?.data?.data || response?.data || response;

                if (Array.isArray(realData)) {
                    setInstitutions(realData);
                } else {
                    setInstitutions([]);
                }
            } catch (err) {
                console.error(err);
            }
        };
        fetchInstitutions();
    }, []);

    const fetchMyLocation = (isManual = false) => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const lat = position.coords.latitude;
                    const lng = position.coords.longitude;

                    setMyLocation({ lat, lng });
                    setMapCenter({ lat, lng });

                    if (mapRef.current) {
                        mapRef.current.setLevel(3);
                        mapRef.current.panTo(new window.kakao.maps.LatLng(lat, lng));
                    }
                },
                () => {
                    if (isManual) alert("위치 권한이 차단되었거나 찾을 수 없습니다.");
                },
                { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
            );
        }
    };

    useEffect(() => {
        fetchMyLocation(false);
    }, []);

    const handleFindMyLocation = () => {
        fetchMyLocation(true);
    };

    const searchPlace = (keyword) => {
        if (!keyword.trim()) {
            alert('검색어를 입력해주세요!');
            return;
        }

        const matchedInst = institutions.find(inst => {
            const name = inst.name || inst.NAME || "";
            return name.includes(keyword);
        });

        if (matchedInst && matchedInst.location && matchedInst.location.coordinates) {
            const lat = matchedInst.location.coordinates[1];
            const lng = matchedInst.location.coordinates[0];

            setMapCenter({ lat, lng });
            setSelectedInst(matchedInst);

            if (mapRef.current) {
                mapRef.current.setLevel(3);
                mapRef.current.panTo(new window.kakao.maps.LatLng(lat, lng));
            }
            return;
        }

        const ps = new window.kakao.maps.services.Places();

        ps.keywordSearch(keyword, (data, status) => {
            if (status === window.kakao.maps.services.Status.OK) {
                const lat = data[0].y;
                const lng = data[0].x;

                setMapCenter({ lat, lng });
                setSelectedInst(null);

                if (mapRef.current) {
                    mapRef.current.setLevel(3);
                    mapRef.current.panTo(new window.kakao.maps.LatLng(lat, lng));
                }
            } else if (status === window.kakao.maps.services.Status.ZERO_RESULT) {
                alert('검색 결과가 존재하지 않습니다.');
            } else if (status === window.kakao.maps.services.Status.ERROR) {
                alert('검색 결과 중 오류가 발생했습니다.');
            }
        });
    };

    return {
        institutions,
        loading,
        error,
        mapCenter,
        mapRef,
        handleFindMyLocation,
        searchPlace,
        selectedInst,
        setSelectedInst,
        myLocation
    };
};