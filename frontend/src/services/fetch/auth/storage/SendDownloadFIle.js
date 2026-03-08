import {API_DOWNLOAD_FILES} from "../../../../UrlConstants.jsx";
import {extractSimpleName} from "../../../util/Utils.js";
import {sendGetObjectStats} from "./SendGetObjectStats.js";
import {throwSpecifyException} from "../../../../exception/ThrowSpecifyException.jsx";

export const sendDownloadFile = async (downloadTask, updateTask, updateDownloadTask, size, updateDownloadSpeed) => {
    const filePath = downloadTask.operation.source;
    const params = new URLSearchParams({path: filePath});
    const fetchUrl = `${API_DOWNLOAD_FILES}?${params.toString()}`;

    const response = await fetch(fetchUrl, {
        method: 'GET',
        credentials: 'include',
    });

    if (response.status === 202) {
        const data = await response.json();
        return {
            async: true,
            ticket: data.ticket
        };
    }

    if (!response.ok) {
        const error = await response.json();
        throwSpecifyException(response.status, error);
        return;
    }

    if (size === 0) {
        try {
            let stats = await sendGetObjectStats(filePath);
            size = stats.size;
        } catch (e) {
            console.log(e);
        }
    }

    const updateSpeed = (speed) => {
        updateDownloadSpeed(downloadTask, speed);
    };

    let loadedSize = 0;
    const reader = response.body.getReader();
    const chunks = [];

    const contentName = filePath.endsWith("/")
        ? extractSimpleName(filePath).replace("/", ".zip")
        : extractSimpleName(filePath);

    let lastLoadedSize = 0;

    const speedInterval = setInterval(() => {
        const speed = (loadedSize - lastLoadedSize);
        lastLoadedSize = loadedSize;
        updateSpeed(speed);
    }, 1000);

    try {
        while (true) {
            const {done, value} = await reader.read();
            if (done) break;

            chunks.push(value);
            loadedSize += value.length;

            if (size > 0) {
                const progress = (loadedSize / size) * 100;
                updateDownloadTask(downloadTask, progress);
            }
        }
    } catch (e) {
        clearInterval(speedInterval);
        throw e;
    }

    clearInterval(speedInterval);

    const blob = new Blob(chunks);
    const url = window.URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', contentName);
    document.body.appendChild(link);
    link.click();

    window.URL.revokeObjectURL(url);
    document.body.removeChild(link);

    updateTask(downloadTask, "completed", "Скачивание завершено");
    return {async: false};
};