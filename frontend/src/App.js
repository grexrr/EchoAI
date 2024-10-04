import React, { useEffect, useState } from 'react';

function App() {
  const [message, setMessage] = useState('');

  useEffect(() => {
    // 替换为后端 API 地址
    fetch('/api/public/hello')
      .then((response) => response.text())
      .then((data) => setMessage(data))
      .catch((error) => console.error('Error fetching data:', error));
  }, []);

  return (
    <div>
      <h1>EchoAI Frontend</h1>
      <p>Backend says: {message}</p>
    </div>
  );
}
export default App;
