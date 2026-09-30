import {
  CheckCircleFilled,
  DownloadOutlined,
  FileTextOutlined,
  LeftOutlined,
  RightOutlined,
  VideoCameraOutlined,
} from "@ant-design/icons";
import { Alert, Button, Card, Empty, Progress, Space, Tag, Typography, message } from "antd";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  portalService,
  type RepresentativeTrainingProgress,
  type Training,
  type TrainingMaterial,
} from "../../services/portalService";

const { Paragraph, Text, Title } = Typography;

function isVideo(material: TrainingMaterial) {
  return material.materialType.toLowerCase() === "video";
}

export function RepresentativeTrainingMaterialsPage() {
  const { trainingId } = useParams();
  const navigate = useNavigate();
  const [training, setTraining] = useState<Training | null>(null);
  const [materials, setMaterials] = useState<TrainingMaterial[]>([]);
  const [progress, setProgress] = useState<RepresentativeTrainingProgress>({
    completedMaterialIds: [],
    completedCount: 0,
    totalCount: 0,
    percentage: 0,
  });
  const [completingMaterialId, setCompletingMaterialId] = useState<string | null>(null);
  const [selectedMaterialId, setSelectedMaterialId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!trainingId) {
      setError("Training not found.");
      setLoading(false);
      return;
    }

    setLoading(true);
    Promise.all([
      portalService.getTraining(trainingId),
      portalService.listRepresentativeTrainingMaterials(trainingId),
      portalService.getRepresentativeTrainingProgress(trainingId),
    ])
      .then(([trainingResponse, materialResponse, progressResponse]) => {
        setTraining(trainingResponse.data);
        setMaterials(materialResponse.data);
        setProgress(progressResponse.data);
        const firstVideo = materialResponse.data.find(isVideo);
        setSelectedMaterialId((firstVideo ?? materialResponse.data[0])?.id ?? null);
        setError(null);
      })
      .catch((loadError: unknown) => {
        const status = typeof loadError === "object" && loadError !== null && "response" in loadError
          ? (loadError as { response?: { status?: number } }).response?.status
          : undefined;
        setError(status === 403
          ? "You need an approved request for this training to view its materials."
          : "Unable to load this training's materials.");
      })
      .finally(() => setLoading(false));
  }, [trainingId]);

  const sortedMaterials = useMemo(
    () => [...materials].sort((first, second) =>
      first.weekNumber - second.weekNumber),
    [materials],
  );
  const materialsByWeek = useMemo(() => sortedMaterials.reduce<Record<number, TrainingMaterial[]>>(
    (grouped, material) => {
      (grouped[material.weekNumber] ??= []).push(material);
      return grouped;
    },
    {},
  ), [sortedMaterials]);
  const selectedMaterial = sortedMaterials.find((material) => material.id === selectedMaterialId) ?? null;
  const selectedIndex = selectedMaterial
    ? sortedMaterials.findIndex((material) => material.id === selectedMaterial.id)
    : -1;
  const completedMaterialIds = new Set(progress.completedMaterialIds);

  async function completeMaterial(material: TrainingMaterial) {
    if (!trainingId || completedMaterialIds.has(material.id)) return;
    setCompletingMaterialId(material.id);
    try {
      const response = await portalService.completeRepresentativeTrainingMaterial(trainingId, material.id);
      setProgress(response.data);
      message.success(isVideo(material) ? "Video lesson completed." : "Material marked as reviewed.");
    } catch {
      message.error("Unable to save your course progress. Please try again.");
    } finally {
      setCompletingMaterialId(null);
    }
  }

  function selectAdjacentMaterial(direction: -1 | 1) {
    const nextMaterial = sortedMaterials[selectedIndex + direction];
    if (nextMaterial) setSelectedMaterialId(nextMaterial.id);
  }

  return (
    <div className="training-course-page">
      <Button
        className="training-course-back"
        icon={<LeftOutlined />}
        onClick={() => navigate("/representative/trainings")}
      >
        Back to trainings
      </Button>
      {error && <Alert message={error} type="error" showIcon />}
      <Card className="training-course-progress">
        <div className="training-course-progress-heading">
          <div>
            <Title level={4}>Your progress</Title>
            <Text type="secondary">
              {progress.completedCount} of {progress.totalCount} materials completed
            </Text>
          </div>
          <Text className="training-course-progress-percent">{progress.percentage}%</Text>
        </div>
        <Progress
          percent={progress.percentage}
          showInfo={false}
          strokeColor="#0e79bf"
          aria-label={`Course progress: ${progress.percentage}%`}
        />
      </Card>
      <div className="training-course-layout">
        <main className="training-course-main">
          <Card className="training-course-player-card" loading={loading}>
            {selectedMaterial ? (
              <>
                {isVideo(selectedMaterial) ? (
                  <div className="training-video-frame">
                    <video
                      key={selectedMaterial.id}
                      className="training-video"
                      controls
                      playsInline
                      preload="metadata"
                      aria-label={selectedMaterial.title}
                      onEnded={() => void completeMaterial(selectedMaterial)}
                    >
                      <source src={selectedMaterial.fileUrl} />
                      Your browser does not support embedded video. Open this lesson in a new tab instead.
                    </video>
                  </div>
                ) : (
                  <div className="training-material-preview">
                    <FileTextOutlined />
                    <Text>{selectedMaterial.materialType === "image" ? "Image material" : "Course material"}</Text>
                    <Button
                      type="primary"
                      icon={<DownloadOutlined />}
                      href={selectedMaterial.fileUrl}
                      target="_blank"
                      rel="noreferrer"
                    >
                      Open material
                    </Button>
                    <Button
                      type={completedMaterialIds.has(selectedMaterial.id) ? "default" : "primary"}
                      icon={<CheckCircleFilled />}
                      loading={completingMaterialId === selectedMaterial.id}
                      disabled={completedMaterialIds.has(selectedMaterial.id)}
                      onClick={() => void completeMaterial(selectedMaterial)}
                    >
                      {completedMaterialIds.has(selectedMaterial.id) ? "Reviewed" : "Mark as reviewed"}
                    </Button>
                  </div>
                )}
                <div className="training-lesson-details">
                  <Space wrap className="training-lesson-meta">
                    <Tag color="blue">Week {selectedMaterial.weekNumber}</Tag>
                    <Text type="secondary">
                      {isVideo(selectedMaterial) ? "Video lesson" : "Course material"}
                    </Text>
                  </Space>
                  <Title level={2}>{selectedMaterial.title}</Title>
                  {selectedMaterial.description && <Paragraph>{selectedMaterial.description}</Paragraph>}
                  <div className="training-lesson-navigation">
                    <Button
                      icon={<LeftOutlined />}
                      disabled={selectedIndex <= 0}
                      onClick={() => selectAdjacentMaterial(-1)}
                    >
                      Previous lesson
                    </Button>
                    <Button
                      type="primary"
                      disabled={selectedIndex < 0 || selectedIndex >= sortedMaterials.length - 1}
                      onClick={() => selectAdjacentMaterial(1)}
                    >
                      Next lesson <RightOutlined />
                    </Button>
                  </div>
                </div>
              </>
            ) : (
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description={loading ? "Loading course materials..." : "No course materials have been uploaded for this training yet."}
              />
            )}
          </Card>
          {training && (
            <Card className="training-course-about" title="About this training">
              <Title level={3}>{training.title}</Title>
              <Paragraph>{training.description}</Paragraph>
            </Card>
          )}
        </main>

        <aside aria-label="Course content">
          <Card
            className="training-course-outline"
            title="Course content"
            extra={<Text type="secondary">{sortedMaterials.length} lessons</Text>}
            loading={loading}
          >
            {sortedMaterials.length === 0 ? (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="No lessons yet" />
            ) : (
              Object.entries(materialsByWeek)
                .sort(([firstWeek], [secondWeek]) => Number(firstWeek) - Number(secondWeek))
                .map(([weekNumber, weekMaterials]) => (
                  <section className="training-course-week" key={weekNumber}>
                    <div className="training-course-week-heading">
                      <Text strong>Week {weekNumber}</Text>
                      <Text type="secondary">{weekMaterials.length} lessons</Text>
                    </div>
                    {weekMaterials.map((material) => {
                      const selected = material.id === selectedMaterial?.id;
                      return (
                        <button
                          className={`training-course-lesson${selected ? " is-selected" : ""}`}
                          key={material.id}
                          type="button"
                          aria-current={selected ? "step" : undefined}
                          onClick={() => setSelectedMaterialId(material.id)}
                        >
                          <span className="training-course-lesson-icon">
                            {isVideo(material) ? <VideoCameraOutlined /> : <FileTextOutlined />}
                          </span>
                          <span className="training-course-lesson-copy">
                            <span className="training-course-lesson-title">{material.title}</span>
                            <span className="training-course-lesson-kind">
                                {completedMaterialIds.has(material.id)
                                  ? "Completed"
                                  : isVideo(material) ? "Video" : "Reading"}
                            </span>
                          </span>
                          {completedMaterialIds.has(material.id) ? (
                            <CheckCircleFilled className="training-course-lesson-completed" />
                          ) : selected ? (
                            <CheckCircleFilled className="training-course-lesson-current" />
                          ) : null}
                        </button>
                      );
                    })}
                  </section>
                ))
            )}
          </Card>
        </aside>
      </div>
    </div>
  );
}
