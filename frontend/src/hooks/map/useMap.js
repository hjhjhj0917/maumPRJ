import { useState, useEffect, useRef, useMemo } from 'react';
import { useKakaoLoader } from 'react-kakao-maps-sdk';
import { getInstitutions } from '../../api/mapApi';

const getDistance = (lat1, lng1, lat2, lng2) => {
    const R = 6371;
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLng = (lng2 - lng1) * Math.PI / 180;
    const a =
        Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLng/2) * Math.sin(dLng/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
};

const formatDistance = (dist) => {
    if (dist < 1) return Math.round(dist * 1000) + 'm';
    return dist.toFixed(1) + 'km';
};

export const useMentalMap = () => {
    const [institutions, setInstitutions] = useState([]);
    const [mapCenter, setMapCenter] = useState({ lat: 37.5665, lng: 126.9780 });
    const [selectedInst, setSelectedInst] = useState(null);
    const [myLocation, setMyLocation] = useState(null);
    const mapRef = useRef(null);

    const [keyword, setKeyword] = useState("");
    const [suggestions, setSuggestions] = useState([]);
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);

    const [selectedCategories, setSelectedCategories] = useState([]);
    const [isFilterOpen, setIsFilterOpen] = useState(false);

    const [mapBounds, setMapBounds] = useState(null);

    const updateMapBounds = (map) => {
        const bounds = map.getBounds();
        setMapBounds(prev => {
            if (prev && prev.toString() === bounds.toString()) {
                return prev;
            }
            return bounds;
        });
    };

    const categories = [
        '전체',
        '공립',
        '광역정신건강복지센터',
        '국립',
        '기초정신건강복지센터',
        '병원',
        '보건소',
        '상급종합병원',
        '의원',
        '자살예방센터',
        '정신요양시설',
        '정신재활시설',
        '종합병원',
        '중독관리통합지원센터'
    ];

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

    const toggleCategory = (category) => {
        if (category === '전체') {
            setSelectedCategories([]);
            return;
        }

        setSelectedCategories(prev =>
            prev.includes(category)
                ? prev.filter(c => c !== category)
                : [...prev, category]
        );
    };

    const filteredInstitutions = useMemo(() => {
        if (selectedCategories.length === 0) return institutions;
        return institutions.filter(inst => {
            const cat = inst.category || inst.CATEGORY || "";
            return selectedCategories.includes(cat.trim());
        });
    }, [institutions, selectedCategories]);

    const visibleInstitutions = useMemo(() => {
        if (!mapBounds) return [];

        const sw = mapBounds.getSouthWest();
        const ne = mapBounds.getNorthEast();

        return filteredInstitutions.filter(inst => {
            if (!inst.location || !inst.location.coordinates) return false;
            const lat = inst.location.coordinates[1];
            const lng = inst.location.coordinates[0];

            return (
                lat >= sw.getLat() && lat <= ne.getLat() &&
                lng >= sw.getLng() && lng <= ne.getLng()
            );
        });
    }, [filteredInstitutions, mapBounds]);


    const searchPlace = (searchKeyword) => {
        if (!searchKeyword.trim()) {
            alert('검색어를 입력해주세요!');
            return;
        }

        const matchedInst = filteredInstitutions.find(inst => {
            const name = inst.name || inst.NAME || "";
            return name.includes(searchKeyword);
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

        ps.keywordSearch(searchKeyword, (data, status) => {
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

    const handleInputChange = (e) => {
        const val = e.target.value;
        setKeyword(val);

        if (!val.trim()) {
            setSuggestions([]);
            setIsDropdownOpen(false);
            return;
        }

        const baseLat = myLocation ? myLocation.lat : mapCenter.lat;
        const baseLng = myLocation ? myLocation.lng : mapCenter.lng;

        const filtered = filteredInstitutions.filter(inst => {
            const name = inst.name || inst.NAME || "";
            return name.includes(val);
        }).map(inst => {
            const lat = inst.location.coordinates[1];
            const lng = inst.location.coordinates[0];
            const distance = getDistance(baseLat, baseLng, lat, lng);
            return { ...inst, distance };
        }).sort((a, b) => a.distance - b.distance)
            .slice(0, 5);

        setSuggestions(filtered);
        setIsDropdownOpen(true);
    };

    const handleSuggestionClick = (inst) => {
        setKeyword(inst.name || inst.NAME);
        setIsDropdownOpen(false);
        setSuggestions([]);
        setSelectedInst(inst);

        if (mapRef.current) {
            mapRef.current.setLevel(3);
            mapRef.current.panTo(new window.kakao.maps.LatLng(
                inst.location.coordinates[1],
                inst.location.coordinates[0]
            ));
        }
    };

    const handleSearch = (e) => {
        e.preventDefault();
        setIsDropdownOpen(false);
        searchPlace(keyword);
    };

    const handleMapClick = () => {
        setSelectedInst(null);
        setIsDropdownOpen(false);
        setIsFilterOpen(false);
    };

    return {
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
    };
};