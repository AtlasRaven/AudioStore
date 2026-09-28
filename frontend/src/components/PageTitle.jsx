export default function PageTitle({ eyebrow, title, detail, action }) {
  return <div className="page-title"><div>{eyebrow && <p className="eyebrow">{eyebrow}</p>}<h1>{title}</h1>{detail && <p>{detail}</p>}</div>{action && <div className="page-title__action">{action}</div>}</div>;
}
