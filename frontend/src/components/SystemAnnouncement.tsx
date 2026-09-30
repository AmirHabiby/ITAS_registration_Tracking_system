import { Alert } from "antd";
import { useEffect, useState } from "react";
import { portalService } from "../services/portalService";

export function SystemAnnouncement() {
  const [text, setText] = useState<string | null>(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    let active = true;
    portalService.getSystemAnnouncement()
      .then((response) => {
        if (active) setText(response.data.text);
      })
      .catch(() => {
        if (active) setError(true);
      });
    return () => {
      active = false;
    };
  }, []);

  if (error) {
    return <Alert type="error" showIcon message="Unable to load system notifications." />;
  }
  if (!text?.trim()) {
    return (
      <Alert
        className="system-announcement"
        type="info"
        showIcon
        message="No current system notification"
        description="There are no system-wide announcements right now."
      />
    );
  }

  return (
    <Alert
      className="system-announcement"
      type="info"
      showIcon
      message="Notification"
      description={<span className="system-announcement__text">{text}</span>}
    />
  );
}
