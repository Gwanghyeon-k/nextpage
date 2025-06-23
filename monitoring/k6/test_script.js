import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '30s', target: 20 }, // 첫번째 스테이지 : 30초동안 사용자 수를 0 -> 20으로 증가
        { duration: '1m', target: 20 },  // 두번째 스테이지 : 1분간 사용자 수를 20으로 유지
        { duration: '10s', target: 0 },  // 세번째 스테이지 : 10초간 사용자 수를 20 -> 0으로 감소
    ],
};

// Base URL
const BASE_URL = 'http://springboot:8080/api/v2';

// Headers
const headers = {
    'Content-Type': 'application/json',
};

export default function () {
    // 루트 스토리 조회
    let rootStoriesRes = http.get(`${BASE_URL}/stories`, { headers });
    check(rootStoriesRes, {
        'getRootStories is status 200': (r) => r.status === 200,
        'getRootStories response time is less than 200ms': (r) => r.timings.duration < 200,
    });

    // 스토리 상세 조회 (storyId = 1 예시)
    let storyDetailsRes = http.get(`${BASE_URL}/stories/details/1`, { headers });
    check(storyDetailsRes, {
        'getStoryDetails is status 200': (r) => r.status === 200,
        'getStoryDetails response time is less than 200ms': (r) => r.timings.duration < 200,
    });

    // 시나리오 조회 (rootId = 1 예시)
    let scenarioRes = http.get(`${BASE_URL}/stories/1`, { headers });
    check(scenarioRes, {
        'getStoriesByRootId is status 200': (r) => r.status === 200,
        'getStoriesByRootId response time is less than 200ms': (r) => r.timings.duration < 200,
    });

    // 특정 분기 조회 (storyId = 1 예시)
    let branchStoriesRes = http.get(`${BASE_URL}/stories/branch/1`, { headers });
    check(branchStoriesRes, {
        'getStoriesByleafId is status 200': (r) => r.status === 200,
        'getStoriesByleafId response time is less than 200ms': (r) => r.timings.duration < 200,
    });

    sleep(1);
}
