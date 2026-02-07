=begin



git init;
git config --local user.name "mole";
git config --local user.email "hyunmin.lim.90@icloud.com";
git switch --create feature-2025;
git remote add origin http://gitlab.opentofu.click/seed-opentofu/backend/java-sso.git;
git add ./; git commit -m "GITLAB 마이그레이션 2026"; git push origin feature-2025;

git remote set-url origin http://gitlab.opentofu.click/seed-opentofu/backend/java-sso.git



=end
